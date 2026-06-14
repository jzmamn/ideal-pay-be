package com.payroll.service;

import com.payroll.entity.*;
import com.payroll.entity.EmployeeSalaryAdvance;
import com.payroll.enums.ComponentType;
import com.payroll.repository.SystemSetupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Core payroll calculation engine.
 *
 * <p>Calculation order:
 * <ol>
 *   <li>Fixed Allowances  — formula or stored amount</li>
 *   <li>Variable Allowances — user-entered amount</li>
 *   <li>Overtime — formula (default: basicSalary/workingDays/8 * hours * 1.5) or stored amount</li>
 *   <li>No Pay — formula (default: basicSalary/workingDays * days) or stored amount</li>
 *   <li>Late Deduction — basicSalary / (workingDays × 8) × lateHours</li>
 *   <li>Fixed Deductions — formula or stored amount</li>
 *   <li>Variable Deductions — user-entered amount</li>
 *   <li>EPF/ETF base → Employee EPF, Employer EPF, ETF (rates from system_setup)</li>
 *   <li>PAYE tax via Sri Lanka annual slab → monthly</li>
 *   <li>Gross Pay, Total Deductions, Net Pay</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryCalculationEngineService {

    private static final List<String> RATE_CODES = List.of(
            "EMPLOYEE_EPF_PERCENT", "EMPLOYER_EPF_PERCENT", "EMPLOYER_ETF_PERCENT",
            "EPF_ENABLED", "ETF_ENABLED", "PAYE_ENABLED");

    private final SystemSetupRepository systemSetupRepo;

    /**
     * Runs the full payroll calculation for a single employee in a given period.
     *
     * @param employee     the employee being processed
     * @param workingDays  total payable days in the pay period (from PayrollPeriod)
     * @param faList       employee fixed allowance entries for the month
     * @param vaList       employee variable allowance entries for the month
     * @param otList       employee overtime entries for the month
     * @param fdList       employee fixed deduction entries for the month
     * @param vdList       employee variable deduction entries for the month
     * @param npList       employee nopay entries for the month
     * @return             {@link SalaryCalculationResult} with all totals and component lines
     */
    public SalaryCalculationResult calculate(
            Employee employee,
            int workingDays,
            List<EmployeeFixedAllowance>    faList,
            List<EmployeeVariableAllowance> vaList,
            List<EmployeeOvertime>          otList,
            List<EmployeeFixedDeduction>    fdList,
            List<EmployeeVariableDeduction> vdList,
            List<EmployeeNopay>             npList,
            List<EmployeeSalaryAdvance>     saList,
            List<EmployeeLate>              lateList) {

        PayrollRates rates = loadRates();

        BigDecimal basicSalary = orZero(employee.getBasicSalary());
        List<ComponentLine> lines = new ArrayList<>();

        log.debug("Payroll calc start — emp={} basicSalary={} workingDays={} epfEnabled={} etfEnabled={} payeEnabled={}",
                employee.getId(), basicSalary, workingDays,
                rates.epfEnabled(), rates.etfEnabled(), rates.payeEnabled());

        // ── STEP A: No-Pay Days — sum employee's total no-pay days for the period ──
        // Used in STEP A2 to proportionally reduce FA amounts where liableNoPay=true.
        BigDecimal totalNopayDays = BigDecimal.ZERO;
        for (EmployeeNopay enp : npList) {
            totalNopayDays = totalNopayDays.add(orZero(enp.getDays()));
        }
        // No-pay proportion: nopayDays / workingDays  (zero when workingDays = 0)
        BigDecimal nopayProportion = (workingDays > 0 && totalNopayDays.compareTo(BigDecimal.ZERO) > 0)
                ? totalNopayDays.divide(BigDecimal.valueOf(workingDays), 10, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // ── STEP A2: Fixed Allowances ─────────────────────────────────────────
        // Formula evaluation happened at load time (PayrollComponentLoadService).
        // This step uses the pre-stored amount and applies no-pay adjustment where applicable.
        //
        // Per spec §"No Pay Processing":
        //   If liableNoPay → adjustedAmt = storedAmt - storedAmt × (nopayDays / workingDays)
        //   Otherwise      → adjustedAmt = storedAmt  (no change)
        //
        // The adjustedAmt is what flows into gross pay and all statutory bases.
        BigDecimal totalFa = BigDecimal.ZERO;
        for (EmployeeFixedAllowance efa : faList) {
            FixedAllowance fa  = efa.getFixedAllowance();
            BigDecimal stored  = orZero(efa.getAmount());
            BigDecimal adjusted;
            if (Boolean.TRUE.equals(fa.getLiableNoPay()) && nopayProportion.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal reduction = stored.multiply(nopayProportion).setScale(2, RoundingMode.HALF_UP);
                adjusted = stored.subtract(reduction).max(BigDecimal.ZERO);
                log.debug("FA [{}] emp={} stored={} nopayReduction={} adjusted={}",
                        fa.getCode(), employee.getId(), stored, reduction, adjusted);
            } else {
                adjusted = stored;
            }
            totalFa = totalFa.add(adjusted);
            lines.add(new ComponentLine(ComponentType.FA, fa.getId(), fa.getCode(), fa.getName(), adjusted, null, null));
        }

        // ── STEP B: Variable Allowances ───────────────────────────────────────
        BigDecimal totalVa = BigDecimal.ZERO;
        for (EmployeeVariableAllowance eva : vaList) {
            VariableAllowance va = eva.getVariableAllowance();
            BigDecimal amt = orZero(eva.getAmount());
            totalVa = totalVa.add(amt);
            lines.add(new ComponentLine(ComponentType.VA, va.getId(), va.getCode(), va.getName(), amt, null, null));
        }

        // ── STEP C: Overtime ──────────────────────────────────────────────────
        // Rate was stored by PayrollComponentLoadService (formula or default).
        // Amount = rate × hours, already persisted in emp_ot.
        // If liableForNopay=true, reduce the OT amount by the nopay proportion
        // (same pattern as fixed allowances in STEP A2).
        BigDecimal totalOt = BigDecimal.ZERO;
        for (EmployeeOvertime eot : otList) {
            Overtime ot = eot.getOvertime();
            BigDecimal stored = orZero(eot.getAmount());
            BigDecimal adjusted;
            if (Boolean.TRUE.equals(ot.getLiableForNopay()) && nopayProportion.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal reduction = stored.multiply(nopayProportion).setScale(2, RoundingMode.HALF_UP);
                adjusted = stored.subtract(reduction).max(BigDecimal.ZERO);
                log.debug("OT [{}] emp={} stored={} nopayReduction={} adjusted={}",
                        ot.getCode(), employee.getId(), stored, reduction, adjusted);
            } else {
                adjusted = stored;
            }
            totalOt = totalOt.add(adjusted);
            lines.add(new ComponentLine(ComponentType.OT, ot.getId(), ot.getCode(), ot.getName(), adjusted, eot.getHours(), null));
        }

        // ── STEP D: No Pay ────────────────────────────────────────────────────
        // Rate was stored by PayrollComponentLoadService. Amount = rate × days,
        // recorded in emp_np by the batch/individual save. Use stored amount only.
        BigDecimal totalNopay = BigDecimal.ZERO;
        for (EmployeeNopay enp : npList) {
            NopayDays nd = enp.getNopayDays();
            BigDecimal amt = orZero(enp.getAmount());
            totalNopay = totalNopay.add(amt);
            lines.add(new ComponentLine(ComponentType.NOPAY, nd.getId(), nd.getCode(), nd.getName(), amt, null, enp.getDays()));
        }

        // ── STEP D2: Late Deduction ───────────────────────────────────────────
        // Rate was stored by PayrollComponentLoadService. Amount = rate × hours,
        // recorded in emp_late by the batch/individual save. Use stored amount only.
        BigDecimal totalLate = BigDecimal.ZERO;
        for (EmployeeLate elate : lateList) {
            BigDecimal amt = orZero(elate.getAmount());
            totalLate = totalLate.add(amt);
            lines.add(new ComponentLine(ComponentType.LATE, null, "LATE", "Late Deduction", amt, elate.getHours(), null));
        }

        // ── STEP E: Fixed Deductions ──────────────────────────────────────────
        // Formula evaluation happens in PayrollComponentLoadService (load phase only).
        // Processing uses the pre-stored amount from emp_fd.
        // If liableNoPay=true, the deduction is proportionally reduced by the no-pay ratio
        // (same pattern as Fixed Allowances in STEP A2).
        BigDecimal totalFd = BigDecimal.ZERO;
        for (EmployeeFixedDeduction efd : fdList) {
            FixedDeduction fd = efd.getFixedDeduction();
            BigDecimal stored = orZero(efd.getAmount());
            BigDecimal amt = Boolean.TRUE.equals(fd.getLiableNoPay()) && nopayProportion.compareTo(BigDecimal.ZERO) > 0
                    ? stored.subtract(stored.multiply(nopayProportion).setScale(2, RoundingMode.HALF_UP)).max(BigDecimal.ZERO)
                    : stored;
            totalFd = totalFd.add(amt);
            lines.add(new ComponentLine(ComponentType.FD, fd.getId(), fd.getCode(), fd.getName(), amt, null, null));
        }

        // ── STEP F: Variable Deductions ───────────────────────────────────────
        BigDecimal totalVd = BigDecimal.ZERO;
        for (EmployeeVariableDeduction evd : vdList) {
            VariableDeduction vd = evd.getVariableDeduction();
            BigDecimal amt = orZero(evd.getAmount());
            totalVd = totalVd.add(amt);
            lines.add(new ComponentLine(ComponentType.VD, vd.getId(), vd.getCode(), vd.getName(), amt, null, null));
        }

        // ── STEP G1: Salary Advance ───────────────────────────────────────────
        BigDecimal totalSa = BigDecimal.ZERO;
        for (EmployeeSalaryAdvance sa : saList) {
            if (Boolean.TRUE.equals(sa.getIsProcessed())) continue; // skip already-processed advances
            BigDecimal amt = orZero(sa.getAmount());
            totalSa = totalSa.add(amt);
            lines.add(new ComponentLine(ComponentType.SA, sa.getId(), "SA", "Salary Advance", amt, null, null));
        }

        // ── STEP G: EPF ───────────────────────────────────────────────────────
        // Per spec §"EPF Earnings Processing":
        //   EPF base = basic + no-pay-adjusted EPF-liable FA + EPF-liable VA + EPF-liable OT
        // Note: the ComponentLine amounts for FA are already the no-pay-adjusted values
        // (computed in STEP A2), so we re-read from the lines map via a helper pass rather
        // than recalculating — simpler to iterate faList and reapply the same adjustment.
        BigDecimal epfBase = basicSalary;

        for (EmployeeFixedAllowance efa : faList) {
            if (!Boolean.TRUE.equals(efa.getFixedAllowance().getLiableForEpf())) continue;
            BigDecimal stored   = orZero(efa.getAmount());
            BigDecimal adjusted = Boolean.TRUE.equals(efa.getFixedAllowance().getLiableNoPay())
                    ? stored.subtract(stored.multiply(nopayProportion).setScale(2, RoundingMode.HALF_UP)).max(BigDecimal.ZERO)
                    : stored;
            epfBase = epfBase.add(adjusted);
        }

        for (EmployeeVariableAllowance eva : vaList)
            if (Boolean.TRUE.equals(eva.getVariableAllowance().getLiableForEpf()))
                epfBase = epfBase.add(orZero(eva.getAmount()));

        for (EmployeeVariableDeduction evd : vdList)
            if (Boolean.TRUE.equals(evd.getVariableDeduction().getLiableForEpf()))
                epfBase = epfBase.subtract(orZero(evd.getAmount()));

        for (EmployeeOvertime eot : otList) {
            if (!Boolean.TRUE.equals(eot.getOvertime().getLiableForEpf())) continue;
            BigDecimal stored = orZero(eot.getAmount());
            BigDecimal adjusted = Boolean.TRUE.equals(eot.getOvertime().getLiableForNopay())
                    ? stored.subtract(stored.multiply(nopayProportion).setScale(2, RoundingMode.HALF_UP)).max(BigDecimal.ZERO)
                    : stored;
            epfBase = epfBase.add(adjusted);
        }

        // Fixed Deductions that are EPF-liable reduce the EPF-contributable base
        for (EmployeeFixedDeduction efd : fdList) {
            FixedDeduction fd = efd.getFixedDeduction();
            if (!Boolean.TRUE.equals(fd.getLiableForEpf())) continue;
            BigDecimal stored = orZero(efd.getAmount());
            BigDecimal adjusted = Boolean.TRUE.equals(fd.getLiableNoPay())
                    ? stored.subtract(stored.multiply(nopayProportion).setScale(2, RoundingMode.HALF_UP)).max(BigDecimal.ZERO)
                    : stored;
            epfBase = epfBase.subtract(adjusted);
        }

        epfBase = epfBase.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        BigDecimal employeeEpf = rates.epfEnabled()
                ? epfBase.multiply(rates.epfEeRate()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal employerEpf = rates.epfEnabled()
                ? epfBase.multiply(rates.epfErRate()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // ── STEP G2: ETF ──────────────────────────────────────────────────────
        // Per spec §"ETF Earnings Processing": ETF base uses liableForEtf on each FA
        // (independent of EPF — a FA may be EPF-liable but not ETF-liable, or vice-versa).
        BigDecimal etfBase = basicSalary;

        for (EmployeeFixedAllowance efa : faList) {
            if (!Boolean.TRUE.equals(efa.getFixedAllowance().getLiableForEtf())) continue;
            BigDecimal stored   = orZero(efa.getAmount());
            BigDecimal adjusted = Boolean.TRUE.equals(efa.getFixedAllowance().getLiableNoPay())
                    ? stored.subtract(stored.multiply(nopayProportion).setScale(2, RoundingMode.HALF_UP)).max(BigDecimal.ZERO)
                    : stored;
            etfBase = etfBase.add(adjusted);
        }

        for (EmployeeVariableAllowance eva : vaList)
            if (Boolean.TRUE.equals(eva.getVariableAllowance().getLiableForEtf()))
                etfBase = etfBase.add(orZero(eva.getAmount()));

        for (EmployeeVariableDeduction evd : vdList)
            if (Boolean.TRUE.equals(evd.getVariableDeduction().getLiableForEtf()))
                etfBase = etfBase.subtract(orZero(evd.getAmount()));

        for (EmployeeOvertime eot : otList) {
            if (!Boolean.TRUE.equals(eot.getOvertime().getLiableForEtf())) continue;
            BigDecimal stored = orZero(eot.getAmount());
            BigDecimal adjusted = Boolean.TRUE.equals(eot.getOvertime().getLiableForNopay())
                    ? stored.subtract(stored.multiply(nopayProportion).setScale(2, RoundingMode.HALF_UP)).max(BigDecimal.ZERO)
                    : stored;
            etfBase = etfBase.add(adjusted);
        }

        // Fixed Deductions that are ETF-liable reduce the ETF base
        for (EmployeeFixedDeduction efd : fdList) {
            FixedDeduction fd = efd.getFixedDeduction();
            if (!Boolean.TRUE.equals(fd.getLiableForEtf())) continue;
            BigDecimal stored = orZero(efd.getAmount());
            BigDecimal adjusted = Boolean.TRUE.equals(fd.getLiableNoPay())
                    ? stored.subtract(stored.multiply(nopayProportion).setScale(2, RoundingMode.HALF_UP)).max(BigDecimal.ZERO)
                    : stored;
            etfBase = etfBase.subtract(adjusted);
        }

        etfBase = etfBase.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        BigDecimal etf = rates.etfEnabled()
                ? etfBase.multiply(rates.etfRate()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        log.debug("EPF base={} empEPF={} erEPF={} ETF base={} ETF={}",
                epfBase, employeeEpf, employerEpf, etfBase, etf);

        if (rates.epfEnabled()) {
            lines.add(new ComponentLine(ComponentType.EPF_EE, null, "EPF_EE",
                    "Employee EPF (" + rates.epfEePct() + "%)", employeeEpf, null, null));
            lines.add(new ComponentLine(ComponentType.EPF_ER, null, "EPF_ER",
                    "Employer EPF (" + rates.epfErPct() + "%)", employerEpf, null, null));
        }
        if (rates.etfEnabled()) {
            lines.add(new ComponentLine(ComponentType.ETF, null, "ETF",
                    "ETF (" + rates.etfPct() + "%)", etf, null, null));
        }

        // ── STEP H: PAYE Tax ──────────────────────────────────────────────────
        // Per spec §"PAYE Earnings Processing":
        //   PAYE base = basic + no-pay-adjusted PAYE-liable FA + PAYE-liable VA + PAYE-liable OT
        //               - employee EPF contribution
        BigDecimal payeBase = basicSalary;

        for (EmployeeFixedAllowance efa : faList) {
            if (!Boolean.TRUE.equals(efa.getFixedAllowance().getLiableForPaye())) continue;
            BigDecimal stored   = orZero(efa.getAmount());
            BigDecimal adjusted = Boolean.TRUE.equals(efa.getFixedAllowance().getLiableNoPay())
                    ? stored.subtract(stored.multiply(nopayProportion).setScale(2, RoundingMode.HALF_UP)).max(BigDecimal.ZERO)
                    : stored;
            payeBase = payeBase.add(adjusted);
        }

        for (EmployeeVariableAllowance eva : vaList)
            if (Boolean.TRUE.equals(eva.getVariableAllowance().getLiableForPaye()))
                payeBase = payeBase.add(orZero(eva.getAmount()));

        for (EmployeeVariableDeduction evd : vdList)
            if (Boolean.TRUE.equals(evd.getVariableDeduction().getLiableForPaye()))
                payeBase = payeBase.subtract(orZero(evd.getAmount()));

        for (EmployeeOvertime eot : otList) {
            if (!Boolean.TRUE.equals(eot.getOvertime().getLiableForPaye())) continue;
            BigDecimal stored = orZero(eot.getAmount());
            BigDecimal adjusted = Boolean.TRUE.equals(eot.getOvertime().getLiableForNopay())
                    ? stored.subtract(stored.multiply(nopayProportion).setScale(2, RoundingMode.HALF_UP)).max(BigDecimal.ZERO)
                    : stored;
            payeBase = payeBase.add(adjusted);
        }

        // Fixed Deductions that are PAYE-liable reduce the PAYE base.
        for (EmployeeFixedDeduction efd : fdList) {
            FixedDeduction fd = efd.getFixedDeduction();
            if (!Boolean.TRUE.equals(fd.getLiableForPaye())) continue;
            BigDecimal stored = orZero(efd.getAmount());
            BigDecimal adjusted = Boolean.TRUE.equals(fd.getLiableNoPay())
                    ? stored.subtract(stored.multiply(nopayProportion).setScale(2, RoundingMode.HALF_UP)).max(BigDecimal.ZERO)
                    : stored;
            payeBase = payeBase.subtract(adjusted);
        }

        payeBase = payeBase.subtract(employeeEpf).max(BigDecimal.ZERO);

        // taxableEarnings: basic + all fixed allowances (nopay-adjusted) + taxable variable allowances
        //                  − taxable variable deductions
        BigDecimal taxableEarnings = basicSalary;
        for (EmployeeFixedAllowance efa : faList) {
            BigDecimal stored = orZero(efa.getAmount());
            BigDecimal adjusted = Boolean.TRUE.equals(efa.getFixedAllowance().getLiableNoPay())
                    ? stored.subtract(stored.multiply(nopayProportion).setScale(2, RoundingMode.HALF_UP)).max(BigDecimal.ZERO)
                    : stored;
            taxableEarnings = taxableEarnings.add(adjusted);
        }
        for (EmployeeVariableAllowance eva : vaList)
            if (Boolean.TRUE.equals(eva.getVariableAllowance().getIsTaxable()))
                taxableEarnings = taxableEarnings.add(orZero(eva.getAmount()));
        for (EmployeeVariableDeduction evd : vdList)
            if (Boolean.TRUE.equals(evd.getVariableDeduction().getIsTaxable()))
                taxableEarnings = taxableEarnings.subtract(orZero(evd.getAmount()));
        taxableEarnings = taxableEarnings.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        BigDecimal payeTax = BigDecimal.ZERO;
        if (rates.payeEnabled()) {
            payeTax = computeMonthlyPaye(payeBase).setScale(2, RoundingMode.HALF_UP);
            log.debug("PAYE base={} tax={}", payeBase, payeTax);
            if (payeTax.compareTo(BigDecimal.ZERO) > 0) {
                lines.add(new ComponentLine(ComponentType.PAYE, null, "PAYE", "PAYE Tax", payeTax, null, null));
            }
        }

        // ── STEP I: Final Totals ──────────────────────────────────────────────
        BigDecimal totalAllowances = totalFa.add(totalVa).add(totalOt);
        BigDecimal totalDeductions = totalNopay.add(totalLate).add(totalFd).add(totalVd)
                .add(totalSa).add(employeeEpf).add(payeTax);
        BigDecimal grossPay = basicSalary.add(totalAllowances);
        BigDecimal netPay   = grossPay.subtract(totalDeductions);

        log.info("Payroll calc done — emp={} gross={} deductions={} net={}",
                employee.getId(), grossPay, totalDeductions, netPay);

        return SalaryCalculationResult.builder()
                .basicSalary(basicSalary)
                .totalAllowances(totalAllowances)
                .totalDeductions(totalDeductions)
                .grossPay(grossPay)
                .netPay(netPay)
                .epfLiableBase(epfBase)
                .taxableEarnings(taxableEarnings)
                .employeeEpf(employeeEpf)
                .employerEpf(employerEpf)
                .etf(etf)
                .payeTax(payeTax)
                .workingDays(workingDays)
                .lines(lines)
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Loads statutory rates and feature flags from {@code system_setup} in a single query.
     * The values are read fresh for every {@link #calculate} call so that admin changes
     * take effect on the next payroll run without requiring a service restart.
     */
    private PayrollRates loadRates() {
        Map<String, String> cfg = systemSetupRepo.findByCodeIn(RATE_CODES).stream()
                .collect(Collectors.toMap(s -> s.getCode(), s -> s.getValue()));

        return new PayrollRates(
                parsePct(cfg, "EMPLOYEE_EPF_PERCENT"),
                parsePct(cfg, "EMPLOYER_EPF_PERCENT"),
                parsePct(cfg, "EMPLOYER_ETF_PERCENT"),
                parseFlag(cfg, "EPF_ENABLED"),
                parseFlag(cfg, "ETF_ENABLED"),
                parseFlag(cfg, "PAYE_ENABLED")
        );
    }

    /** Reads a percentage value (e.g. "8.00") and converts it to a decimal rate (0.08). */
    private BigDecimal parsePct(Map<String, String> cfg, String code) {
        String raw = cfg.get(code);
        if (raw == null) throw new IllegalStateException("Missing system_setup entry: " + code);
        return new BigDecimal(raw).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
    }

    /** Reads a Y/N flag; defaults to {@code true} (enabled) when the key is absent. */
    private boolean parseFlag(Map<String, String> cfg, String code) {
        String raw = cfg.get(code);
        return raw == null || "Y".equalsIgnoreCase(raw.trim());
    }

    private BigDecimal orZero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    /**
     * Sri Lanka PAYE tax — annualises the monthly base, applies progressive slabs,
     * then returns the monthly portion.
     *
     * Slabs (as of 2024 Budget):
     *   First  1,200,000  — 0%
     *   Next     500,000  — 6%
     *   Next     800,000  — 12%
     *   Next   1,000,000  — 18%
     *   Next   1,500,000  — 24%
     *   Above  5,000,000  — 36%
     */
    private BigDecimal computeMonthlyPaye(BigDecimal monthlyBase) {
        if (monthlyBase.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        BigDecimal annual = monthlyBase.multiply(BigDecimal.valueOf(12));
        BigDecimal annualTax = BigDecimal.ZERO;

        // { slab ceiling (null = unlimited), rate }
        Object[][] slabs = {
            { new BigDecimal("1200000"), new BigDecimal("0.00") },
            { new BigDecimal("500000"),  new BigDecimal("0.06") },
            { new BigDecimal("800000"),  new BigDecimal("0.12") },
            { new BigDecimal("1000000"), new BigDecimal("0.18") },
            { new BigDecimal("1500000"), new BigDecimal("0.24") },
            { null,                      new BigDecimal("0.36") },
        };

        BigDecimal remaining = annual;
        for (Object[] slab : slabs) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal slabSize = (BigDecimal) slab[0];
            BigDecimal rate     = (BigDecimal) slab[1];
            if (slabSize == null) {
                annualTax = annualTax.add(remaining.multiply(rate));
                break;
            }
            BigDecimal taxable = remaining.min(slabSize);
            annualTax = annualTax.add(taxable.multiply(rate));
            remaining = remaining.subtract(slabSize);
        }

        return annualTax.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
    }
}
