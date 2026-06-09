package com.payroll.service;

import com.payroll.entity.*;
import com.payroll.entity.EmployeeSalaryAdvance;
import com.payroll.enums.ComponentType;
import com.payroll.formula.PayrollContextBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
 *   <li>EPF/ETF base → Employee EPF (8%), Employer EPF (12%), ETF (3%)</li>
 *   <li>PAYE tax via Sri Lanka annual slab → monthly</li>
 *   <li>Gross Pay, Total Deductions, Net Pay</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryCalculationEngineService {

    private final FormulaEngineService formulaEngineService;

    private static final BigDecimal EPF_EE_RATE = new BigDecimal("0.08");
    private static final BigDecimal EPF_ER_RATE = new BigDecimal("0.12");
    private static final BigDecimal ETF_RATE    = new BigDecimal("0.03");

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

        BigDecimal basicSalary = orZero(employee.getBasicSalary());
        List<ComponentLine> lines = new ArrayList<>();

        log.debug("Payroll calc start — emp={} basicSalary={} workingDays={}",
                employee.getId(), basicSalary, workingDays);

        // Build full MVEL context once; updated amounts written back to entities so
        // the context always reflects the latest computed values.
        Map<String, Object> ctx = PayrollContextBuilder.builder()
                .employee(employee)
                .workingDays(workingDays)
                .fixedAllowances(faList)
                .variableAllowances(vaList)
                .overtimeEntries(otList)
                .nopayEntries(npList)
                .lateEntries(lateList)
                .fixedDeductions(fdList)
                .variableDeductions(vdList)
                .build();

        // ── STEP A: Fixed Allowances ──────────────────────────────────────────
        BigDecimal totalFa = BigDecimal.ZERO;
        for (EmployeeFixedAllowance efa : faList) {
            FixedAllowance fa = efa.getFixedAllowance();
            BigDecimal amt = resolveAmount(fa.getFormulaEnabled(), fa.getFormula(), efa.getAmount(), ctx, "FA " + fa.getCode());
            efa.setAmount(amt);
            totalFa = totalFa.add(amt);
            lines.add(new ComponentLine(ComponentType.FA, fa.getId(), fa.getCode(), fa.getName(), amt, null, null));
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
        BigDecimal totalOt = BigDecimal.ZERO;
        for (EmployeeOvertime eot : otList) {
            Overtime ot = eot.getOvertime();
            BigDecimal amt;
            if (Boolean.TRUE.equals(ot.getFormulaEnabled()) && ot.getFormula() != null && !ot.getFormula().isBlank()) {
                amt = formulaEngineService.evaluate(ot.getFormula(), ctx);
                log.debug("OT formula [{}] → {}", ot.getCode(), amt);
            } else {
                // Default: basicSalary / workingDays / 8 * hours * 1.5
                BigDecimal hours = orZero(eot.getHours());
                if (workingDays == 0 || hours.compareTo(BigDecimal.ZERO) == 0) {
                    amt = BigDecimal.ZERO;
                } else {
                    amt = basicSalary
                            .divide(BigDecimal.valueOf(workingDays), 10, RoundingMode.HALF_UP)
                            .divide(BigDecimal.valueOf(8), 10, RoundingMode.HALF_UP)
                            .multiply(hours)
                            .multiply(new BigDecimal("1.5"))
                            .setScale(2, RoundingMode.HALF_UP);
                }
                log.debug("OT default calc [{}] hours={} → {}", ot.getCode(), hours, amt);
            }
            eot.setAmount(amt);
            totalOt = totalOt.add(amt);
            lines.add(new ComponentLine(ComponentType.OT, ot.getId(), ot.getCode(), ot.getName(), amt, eot.getHours(), null));
        }

        // ── STEP D: No Pay ────────────────────────────────────────────────────
        BigDecimal totalNopay = BigDecimal.ZERO;
        for (EmployeeNopay enp : npList) {
            NopayDays nd = enp.getNopayDays();
            BigDecimal amt;
            if (Boolean.TRUE.equals(nd.getFormulaEnabled()) && nd.getFormula() != null && !nd.getFormula().isBlank()) {
                amt = formulaEngineService.evaluate(nd.getFormula(), ctx);
                log.debug("Nopay formula [{}] → {}", nd.getCode(), amt);
            } else {
                // Default: basicSalary / workingDays * days
                BigDecimal days = orZero(enp.getDays());
                amt = workingDays == 0 ? BigDecimal.ZERO
                        : basicSalary
                                .divide(BigDecimal.valueOf(workingDays), 10, RoundingMode.HALF_UP)
                                .multiply(days)
                                .setScale(2, RoundingMode.HALF_UP);
                log.debug("Nopay default calc [{}] days={} → {}", nd.getCode(), days, amt);
            }
            enp.setAmount(amt);
            totalNopay = totalNopay.add(amt);
            lines.add(new ComponentLine(ComponentType.NOPAY, nd.getId(), nd.getCode(), nd.getName(), amt, null, enp.getDays()));
        }

        // ── STEP D2: Late Deduction ───────────────────────────────────────────
        // Formula: basicSalary / (workingDays × 8) × lateHours
        BigDecimal totalLate = BigDecimal.ZERO;
        for (EmployeeLate elate : lateList) {
            BigDecimal lateHours = orZero(elate.getHours());
            BigDecimal amt;
            if (workingDays == 0 || lateHours.compareTo(BigDecimal.ZERO) == 0) {
                amt = BigDecimal.ZERO;
            } else {
                amt = basicSalary
                        .divide(BigDecimal.valueOf(workingDays), 10, RoundingMode.HALF_UP)
                        .divide(BigDecimal.valueOf(8), 10, RoundingMode.HALF_UP)
                        .multiply(lateHours)
                        .setScale(2, RoundingMode.HALF_UP);
            }
            elate.setAmount(amt);
            totalLate = totalLate.add(amt);
            log.debug("Late deduction hours={} → {}", lateHours, amt);
            lines.add(new ComponentLine(ComponentType.LATE, null, "LATE", "Late Deduction", amt, lateHours, null));
        }

        // ── STEP E: Fixed Deductions ──────────────────────────────────────────
        BigDecimal totalFd = BigDecimal.ZERO;
        for (EmployeeFixedDeduction efd : fdList) {
            FixedDeduction fd = efd.getFixedDeduction();
            BigDecimal amt = resolveAmount(fd.getFormulaEnabled(), fd.getFormula(), efd.getAmount(), ctx, "FD " + fd.getCode());
            efd.setAmount(amt);
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

        // ── STEP G: EPF / ETF ─────────────────────────────────────────────────
        // Base = basic + EPF-liable FA + EPF-liable VA + EPF-liable OT - EPF-liable nopay
        BigDecimal epfBase = basicSalary;

        for (EmployeeFixedAllowance efa : faList)
            if (Boolean.TRUE.equals(efa.getFixedAllowance().getLiableForEpf()))
                epfBase = epfBase.add(orZero(efa.getAmount()));

        for (EmployeeVariableAllowance eva : vaList)
            if (Boolean.TRUE.equals(eva.getVariableAllowance().getLiableForEpf()))
                epfBase = epfBase.add(orZero(eva.getAmount()));

        for (EmployeeOvertime eot : otList)
            if (Boolean.TRUE.equals(eot.getOvertime().getLiableForEpf()))
                epfBase = epfBase.add(orZero(eot.getAmount()));

        for (EmployeeNopay enp : npList)
            if (Boolean.TRUE.equals(enp.getNopayDays().getLiableNoPay()))
                epfBase = epfBase.subtract(orZero(enp.getAmount()));

        epfBase = epfBase.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        BigDecimal employeeEpf = epfBase.multiply(EPF_EE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal employerEpf = epfBase.multiply(EPF_ER_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal etf         = epfBase.multiply(ETF_RATE).setScale(2, RoundingMode.HALF_UP);

        log.debug("EPF base={} empEPF={} erEPF={} ETF={}", epfBase, employeeEpf, employerEpf, etf);

        lines.add(new ComponentLine(ComponentType.EPF_EE, null, "EPF_EE", "Employee EPF (8%)",  employeeEpf, null, null));
        lines.add(new ComponentLine(ComponentType.EPF_ER, null, "EPF_ER", "Employer EPF (12%)", employerEpf, null, null));
        lines.add(new ComponentLine(ComponentType.ETF,    null, "ETF",    "ETF (3%)",           etf,         null, null));

        // ── STEP H: PAYE Tax ──────────────────────────────────────────────────
        // Base = basic + PAYE-liable FA + PAYE-liable VA + PAYE-liable OT - employee EPF
        BigDecimal payeBase = basicSalary;

        for (EmployeeFixedAllowance efa : faList)
            if (Boolean.TRUE.equals(efa.getFixedAllowance().getLiableForPaye()))
                payeBase = payeBase.add(orZero(efa.getAmount()));

        for (EmployeeVariableAllowance eva : vaList)
            if (Boolean.TRUE.equals(eva.getVariableAllowance().getLiableForPaye()))
                payeBase = payeBase.add(orZero(eva.getAmount()));

        for (EmployeeOvertime eot : otList)
            if (Boolean.TRUE.equals(eot.getOvertime().getLiableForPaye()))
                payeBase = payeBase.add(orZero(eot.getAmount()));

        payeBase = payeBase.subtract(employeeEpf).max(BigDecimal.ZERO);

        BigDecimal payeTax = computeMonthlyPaye(payeBase).setScale(2, RoundingMode.HALF_UP);
        log.debug("PAYE base={} tax={}", payeBase, payeTax);

        if (payeTax.compareTo(BigDecimal.ZERO) > 0) {
            lines.add(new ComponentLine(ComponentType.PAYE, null, "PAYE", "PAYE Tax", payeTax, null, null));
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
                .employeeEpf(employeeEpf)
                .employerEpf(employerEpf)
                .etf(etf)
                .payeTax(payeTax)
                .workingDays(workingDays)
                .lines(lines)
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BigDecimal resolveAmount(Boolean formulaEnabled, String formula,
                                     BigDecimal fallback, Map<String, Object> ctx, String label) {
        if (Boolean.TRUE.equals(formulaEnabled) && formula != null && !formula.isBlank()) {
            BigDecimal result = formulaEngineService.evaluate(formula, ctx);
            log.debug("Formula [{}] evaluated → {}", label, result);
            return result;
        }
        return orZero(fallback);
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
