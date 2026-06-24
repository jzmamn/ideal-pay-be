package com.payroll.service.impl;

import com.payroll.dto.response.LoadSummaryDTO;
import com.payroll.entity.*;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.formula.PayrollContextBuilder;
import com.payroll.entity.PayrollPeriod;
import com.payroll.repository.*;
import com.payroll.service.FormulaEngineService;
import com.payroll.service.PayrollComponentLoadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default implementation of {@link PayrollComponentLoadService}.
 *
 * <p>Each component type is processed in its own private method.
 * Errors for individual employees are caught, recorded in the summary, and
 * the loop continues — a misconfigured formula for one employee does not
 * block others.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollComponentLoadServiceImpl implements PayrollComponentLoadService {

    // ── Master component repositories ────────────────────────────────────────
    private final FixedAllowanceRepository       faRepository;
    private final FixedDeductionRepository       fdRepository;
    private final OvertimeRepository             otRepository;
    private final NopayRepository                nopayRepository;
    private final LateDeductionConfigRepository  lateConfigRepository;
    private final BonusRepository                bonusRepository;

    // ── Employee transaction repositories ────────────────────────────────────
    private final EmployeeFixedAllowanceRepository    empFaRepository;
    private final EmployeeFixedDeductionRepository    empFdRepository;
    private final EmployeeOvertimeRepository          empOtRepository;
    private final EmployeeNopayRepository             empNpRepository;
    private final EmployeeLateRepository              empLateRepository;
    private final EmployeeBonusRepository             empBonusRepository;

    // ── Other repositories ───────────────────────────────────────────────────
    private final EmployeeRepository    employeeRepository;
    private final UsrRepository         usrRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;

    // ── Formula engine ───────────────────────────────────────────────────────
    private final FormulaEngineService formulaEngineService;

    // ── Global config ────────────────────────────────────────────────────────
    private final com.payroll.service.SystemSetupService systemSetupService;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LoadSummaryDTO loadForPeriod(int month, int year, Long userId) {
        String payrollMonth = toPayrollMonth(month, year);
        int workingDays = resolveWorkingDays(month, year);
        log.info("Loading payroll components for period {} (workingDays={})", payrollMonth, workingDays);

        Usr user = resolveUser(userId);
        List<Employee> employees = employeeRepository.findAllByIsActive(true, Sort.by("id"));

        LoadSummaryDTO.LoadSummaryDTOBuilder summary = LoadSummaryDTO.builder();
        AtomicInteger upserted = new AtomicInteger();
        List<String> errors = new java.util.ArrayList<>();

        for (Employee emp : employees) {
            try {
                upserted.addAndGet(loadSingleEmployee(emp, payrollMonth, workingDays, user));
            } catch (Exception ex) {
                String msg = "EMP-" + emp.getEmployeeNo() + ": " + ex.getMessage();
                log.warn("Load error — {}", msg, ex);
                errors.add(msg);
            }
        }

        return summary
                .employeesProcessed(employees.size())
                .recordsUpserted(upserted.get())
                .errors(errors)
                .build();
    }

    @Override
    @Transactional
    public LoadSummaryDTO loadForEmployee(Long empId, int month, int year, Long userId) {
        String payrollMonth = toPayrollMonth(month, year);
        int workingDays = resolveWorkingDays(month, year);
        log.info("Loading payroll components for emp={} period={} (workingDays={})", empId, payrollMonth, workingDays);

        Usr user = resolveUser(userId);
        Employee emp = employeeRepository.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", empId));

        int upserted = loadSingleEmployee(emp, payrollMonth, workingDays, user);

        return LoadSummaryDTO.builder()
                .employeesProcessed(1)
                .recordsUpserted(upserted)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Core per-employee load
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Loads all component types for a single employee. Returns the number of records upserted.
     */
    private int loadSingleEmployee(Employee emp, String payrollMonth, int workingDays, Usr user) {
        BigDecimal basicSalary = orZero(emp.getBasicSalary());
        Map<String, Object> ctx = PayrollContextBuilder.builder()
                .employee(emp)
                .workingDays(workingDays)
                .build();

        int count = 0;
        count += loadFa(emp, payrollMonth, ctx, user);
        count += loadFd(emp, payrollMonth, ctx, user);
        count += loadOt(emp, payrollMonth, basicSalary, workingDays, ctx, user);
        count += loadNp(emp, payrollMonth, basicSalary, workingDays, ctx, user);
        count += loadLate(emp, payrollMonth, basicSalary, ctx, user);
        count += loadBonus(emp, payrollMonth, ctx, user);
        return count;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fixed Allowances
    // ─────────────────────────────────────────────────────────────────────────

    private int loadFa(Employee emp, String payrollMonth, Map<String, Object> ctx, Usr user) {
        List<FixedAllowance> activeList = faRepository.findByIsActiveTrue();
        int count = 0;
        for (FixedAllowance fa : activeList) {
            boolean alreadyAssignedThisMonth = empFaRepository
                    .findByEmployee_IdAndFixedAllowance_IdAndPayrollMonth(emp.getId(), fa.getId(), payrollMonth)
                    .isPresent();

            // Fixed Allowances are no longer auto-assigned to every employee. Only carry the
            // component forward into a new month if the employee has explicitly selected it
            // at least once via the Employee → Salary Tab → Fixed Allowance screen (i.e. an
            // EmployeeFixedAllowance row exists for this employee/allowance in some month).
            if (!alreadyAssignedThisMonth
                    && !empFaRepository.existsByEmployee_IdAndFixedAllowance_Id(emp.getId(), fa.getId())) {
                continue;
            }

            // Formula present → evaluate MVEL formula now (load phase only, never during execution)
            // No formula → use the static company-level amount (fa.amount), if set
            boolean fromFormula = fa.getFormula() != null && !fa.getFormula().isBlank();

            BigDecimal amt = resolveConfiguredAmount(
                    fa.getFormula(), fa.getAmount(), ctx,
                    "FA/" + fa.getCode());
            if (amt == null) continue; // neither formula nor static amount configured — skip

            final BigDecimal finalAmt       = amt;
            final boolean   finalFromFormula = fromFormula;
            empFaRepository.findByEmployee_IdAndFixedAllowance_IdAndPayrollMonth(
                    emp.getId(), fa.getId(), payrollMonth)
                    .ifPresentOrElse(
                            existing -> {
                                existing.setAmount(finalAmt);
                                existing.setFormulaCalculated(finalFromFormula);
                                existing.setModifiedBy(user);
                                empFaRepository.save(existing);
                            },
                            () -> empFaRepository.save(EmployeeFixedAllowance.builder()
                                    .employee(emp)
                                    .fixedAllowance(fa)
                                    .amount(finalAmt)
                                    .formulaCalculated(finalFromFormula)
                                    .payrollMonth(payrollMonth)
                                    .isProcessed(false)
                                    .createdBy(user)
                                    .modifiedBy(user)
                                    .build())
                    );
            count++;
        }
        return count;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fixed Deductions
    // ─────────────────────────────────────────────────────────────────────────

    private int loadFd(Employee emp, String payrollMonth, Map<String, Object> ctx, Usr user) {
        List<FixedDeduction> activeList = fdRepository.findAllByIsActive(true, Sort.by("id"));
        int count = 0;
        for (FixedDeduction fd : activeList) {
            if (fd.getFormula() == null || fd.getFormula().isBlank()) continue;

            boolean alreadyAssignedThisMonth = empFdRepository
                    .findByEmployee_IdAndFixedDeduction_IdAndPayrollMonth(emp.getId(), fd.getId(), payrollMonth)
                    .isPresent();

            // Fixed Deductions are not auto-assigned to every employee. Only carry the
            // component forward into a new month if the employee has explicitly selected it
            // at least once via the Employee → Salary Tab → Fixed Deduction screen (i.e. an
            // EmployeeFixedDeduction row exists for this employee/deduction in some month).
            if (!alreadyAssignedThisMonth
                    && !empFdRepository.existsByEmployee_IdAndFixedDeduction_Id(emp.getId(), fd.getId())) {
                continue;
            }

            BigDecimal amt = orZero(formulaEngineService.evaluate(fd.getFormula(), ctx));
            log.debug("Formula [FD/{}] → {}", fd.getCode(), amt);

            final BigDecimal finalAmt = amt;
            empFdRepository.findByEmployee_IdAndFixedDeduction_IdAndPayrollMonth(
                    emp.getId(), fd.getId(), payrollMonth)
                    .ifPresentOrElse(
                            existing -> {
                                existing.setAmount(finalAmt);
                                existing.setFormulaCalculated(true);
                                existing.setModifiedBy(user);
                                empFdRepository.save(existing);
                            },
                            () -> empFdRepository.save(EmployeeFixedDeduction.builder()
                                    .employee(emp)
                                    .fixedDeduction(fd)
                                    .amount(finalAmt)
                                    .formulaCalculated(true)
                                    .payrollMonth(payrollMonth)
                                    .isProcessed(false)
                                    .createdBy(user)
                                    .modifiedBy(user)
                                    .build())
                    );
            count++;
        }
        return count;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Overtime — stores RATE; amount = rate × hours
    // ─────────────────────────────────────────────────────────────────────────

    private int loadOt(Employee emp, String payrollMonth, BigDecimal basicSalary,
                       int workingDays, Map<String, Object> ctx, Usr user) {
        List<Overtime> activeList = otRepository.findAllByIsActive(true, Sort.by("id"));
        int count = 0;

        for (Overtime ot : activeList) {
            BigDecimal rate;
            if (ot.getFormula() != null && !ot.getFormula().isBlank()) {
                rate = formulaEngineService.evaluate(ot.getFormula(), ctx);
                log.debug("OT rate formula [{}] emp={} → {}", ot.getCode(), emp.getId(), rate);
            } else {
                // Default: basicSalary / (workingDays × 8)
                rate = workingDays > 0
                        ? basicSalary.divide(BigDecimal.valueOf(workingDays), 10, RoundingMode.HALF_UP)
                                     .divide(BigDecimal.valueOf(8), 6, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
            }

            final BigDecimal finalRate = orZero(rate);
            empOtRepository.findByEmployee_IdAndOvertime_IdAndPayrollMonth(
                    emp.getId(), ot.getId(), payrollMonth)
                    .ifPresentOrElse(
                            existing -> {
                                BigDecimal hours = orZero(existing.getHours());
                                existing.setRate(finalRate);
                                existing.setAmount(finalRate.multiply(hours).setScale(2, RoundingMode.HALF_UP));
                                existing.setModifiedBy(user);
                                empOtRepository.save(existing);
                            },
                            () -> empOtRepository.save(EmployeeOvertime.builder()
                                    .employee(emp)
                                    .overtime(ot)
                                    .rate(finalRate)
                                    .hours(BigDecimal.ZERO)
                                    .amount(BigDecimal.ZERO)
                                    .payrollMonth(payrollMonth)
                                    .isProcessed(false)
                                    .createdBy(user)
                                    .modifiedBy(user)
                                    .build())
                    );
            count++;
        }
        return count;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // No Pay — stores RATE; amount = rate × days
    // Loops all active Nopay types; rate denominator = employee.nopayDays.days
    // (the NoPay days config assigned to the employee on employee.nopay_days_id).
    // ─────────────────────────────────────────────────────────────────────────

    private int loadNp(Employee emp, String payrollMonth, BigDecimal basicSalary,
                       int workingDays, Map<String, Object> ctx, Usr user) {
        List<Nopay> activeList = nopayRepository.findAllByIsActive(true, Sort.by("id"));
        int count = 0;

        // Denominator for the daily rate: the NoPay days config on the employee
        // (e.g. 26 working days). Falls back to the period's workingDays if unset.
        NopayDays nopayDaysCfg = emp.getNopayDays();
        BigDecimal ruleDays = (nopayDaysCfg != null) ? nopayDaysCfg.getDays() : null;
        BigDecimal denominator = (ruleDays != null && ruleDays.compareTo(BigDecimal.ZERO) > 0)
                ? ruleDays
                : BigDecimal.valueOf(workingDays);

        for (Nopay np : activeList) {
            BigDecimal rate;
            if (np.getFormula() != null && !np.getFormula().isBlank()) {
                rate = formulaEngineService.evaluate(np.getFormula(), ctx);
                log.debug("NP rate formula [{}] emp={} → {}", np.getCode(), emp.getId(), rate);
            } else {
                rate = denominator.compareTo(BigDecimal.ZERO) > 0
                        ? basicSalary.divide(denominator, 6, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
            }

            final BigDecimal finalRate = orZero(rate);
            empNpRepository.findByEmployee_IdAndNopay_IdAndPayrollMonth(
                    emp.getId(), np.getId(), payrollMonth)
                    .ifPresentOrElse(
                            existing -> {
                                BigDecimal days = orZero(existing.getDays());
                                existing.setRate(finalRate);
                                existing.setAmount(finalRate.multiply(days).setScale(2, RoundingMode.HALF_UP));
                                existing.setModifiedBy(user);
                                empNpRepository.save(existing);
                            },
                            () -> empNpRepository.save(EmployeeNopay.builder()
                                    .employee(emp)
                                    .nopay(np)
                                    .rate(finalRate)
                                    .days(BigDecimal.ZERO)
                                    .amount(BigDecimal.ZERO)
                                    .payrollMonth(payrollMonth)
                                    .isProcessed(false)
                                    .createdBy(user)
                                    .modifiedBy(user)
                                    .build())
                    );
            count++;
        }
        return count;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Late Deduction — singleton config; one emp_late row per (employee, month).
    // Rate = basicSalary / (workingDays × workingHoursPerDay), or custom formula.
    // ─────────────────────────────────────────────────────────────────────────

    private int loadLate(Employee emp, String payrollMonth, BigDecimal basicSalary,
                         Map<String, Object> ctx, Usr user) {
        // Singleton: use the first user-created config (id > 0).
        // Fall back to system defaults when no config has been saved yet.
        LateDeductionConfig config = lateConfigRepository
                .findTopByIdGreaterThanOrderByIdAsc(0L)
                .orElse(null);

        BigDecimal rate;
        if (config != null && config.getFormula() != null && !config.getFormula().isBlank()) {
            rate = formulaEngineService.evaluate(config.getFormula(), ctx);
            log.debug("Late rate formula emp={} → {}", emp.getId(), rate);
        } else {
            int wd  = config != null && config.getWorkingDays() != null
                      ? config.getWorkingDays() : systemSetupService.getWorkingDays();
            int hpd = config != null && config.getWorkingHoursPerDay() != null
                      ? config.getWorkingHoursPerDay() : 8;
            int denominator = wd * hpd;
            rate = denominator > 0
                    ? basicSalary.divide(BigDecimal.valueOf(denominator), 6, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
        }

        final BigDecimal          finalRate   = orZero(rate);
        final LateDeductionConfig finalConfig = config;

        // One row per (employee, month) — use the simple two-key lookup to avoid
        // late_config_id dependency and prevent duplicate rows from the old multi-config path.
        empLateRepository.findByEmployee_IdAndPayrollMonth(emp.getId(), payrollMonth)
                .ifPresentOrElse(
                        existing -> {
                            BigDecimal hours = orZero(existing.getHours());
                            existing.setRate(finalRate);
                            existing.setLateConfig(finalConfig);
                            existing.setAmount(finalRate.multiply(hours)
                                    .setScale(2, RoundingMode.HALF_UP));
                            existing.setModifiedBy(user);
                            empLateRepository.save(existing);
                        },
                        () -> empLateRepository.save(EmployeeLate.builder()
                                .employee(emp)
                                .lateConfig(finalConfig)
                                .rate(finalRate)
                                .hours(BigDecimal.ZERO)
                                .amount(BigDecimal.ZERO)
                                .payrollMonth(payrollMonth)
                                .isProcessed(false)
                                .createdBy(user)
                                .modifiedBy(user)
                                .build())
                );
        return 1;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bonus — stores amount (editable after load)
    // ─────────────────────────────────────────────────────────────────────────

    private int loadBonus(Employee emp, String payrollMonth, Map<String, Object> ctx, Usr user) {
        List<Bonus> activeList = bonusRepository.findAllByIsActive(true, Sort.by("id"));
        int count = 0;
        for (Bonus bonus : activeList) {
            BigDecimal amt = resolveConfiguredAmount(
                    bonus.getFormula(), null, ctx,
                    "Bonus/" + bonus.getCode());
            if (amt == null) continue;

            final BigDecimal finalAmt = amt;
            empBonusRepository.findByEmployeeIdAndPayrollMonthAndBonusId(
                    emp.getId(), payrollMonth, bonus.getId())
                    .ifPresentOrElse(
                            existing -> {
                                existing.setAmount(finalAmt);
                                existing.setModifiedBy(user);
                                empBonusRepository.save(existing);
                            },
                            () -> empBonusRepository.save(EmployeeBonus.builder()
                                    .employee(emp)
                                    .bonus(bonus)
                                    .amount(finalAmt)
                                    .payrollMonth(payrollMonth)
                                    .isProcessed(false)
                                    .createdBy(user)
                                    .modifiedBy(user)
                                    .build())
                    );
            count++;
        }
        return count;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Evaluates a formula (if non-blank) or falls back to a static amount.
     * Returns {@code null} when neither a formula nor a positive static amount is configured,
     * signalling that this component should be skipped for this employee.
     */
    private BigDecimal resolveConfiguredAmount(String formula, BigDecimal staticAmount,
                                               Map<String, Object> ctx, String label) {
        if (formula != null && !formula.isBlank()) {
            BigDecimal result = formulaEngineService.evaluate(formula, ctx);
            log.debug("Formula [{}] → {}", label, result);
            return orZero(result);
        }
        if (staticAmount != null && staticAmount.compareTo(BigDecimal.ZERO) > 0) {
            return staticAmount;
        }
        return null; // nothing configured
    }

    /**
     * Looks up {@code PayrollPeriod.workingDays} for the given month/year.
     * Falls back to the global system_setup WORKING_DAYS value if no period
     * record exists yet (e.g. period not yet created).
     */
    private int resolveWorkingDays(int month, int year) {
        return payrollPeriodRepository
                .findFirstByPeriodYearAndPeriodMonth(year, month)
                .map(PayrollPeriod::getWorkingDays)
                .filter(wd -> wd != null && wd > 0)
                .orElseGet(() -> {
                    int fallback = systemSetupService.getWorkingDays();
                    log.warn("No PayrollPeriod found for {}-{} — defaulting workingDays to system_setup value ({})",
                            year, String.format("%02d", month), fallback);
                    return fallback;
                });
    }

    private static String toPayrollMonth(int month, int year) {
        return String.format("%d-%02d", year, month);
    }

    private Usr resolveUser(Long userId) {
        return usrRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private static BigDecimal orZero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
