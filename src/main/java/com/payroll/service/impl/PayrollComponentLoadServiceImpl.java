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
    private final NopayDaysRepository            npRepository;
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
            BigDecimal amt = resolveConfiguredAmount(
                    fa.getFormulaEnabled(), fa.getFormula(), null, ctx,
                    "FA/" + fa.getCode());
            if (amt == null) continue; // no formula or amount configured — skip

            final BigDecimal finalAmt = amt;
            empFaRepository.findByEmployee_IdAndFixedAllowance_IdAndPayrollMonth(
                    emp.getId(), fa.getId(), payrollMonth)
                    .ifPresentOrElse(
                            existing -> {
                                existing.setAmount(finalAmt);
                                existing.setModifiedBy(user);
                                empFaRepository.save(existing);
                            },
                            () -> empFaRepository.save(EmployeeFixedAllowance.builder()
                                    .employee(emp)
                                    .fixedAllowance(fa)
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
    // Fixed Deductions
    // ─────────────────────────────────────────────────────────────────────────

    private int loadFd(Employee emp, String payrollMonth, Map<String, Object> ctx, Usr user) {
        List<FixedDeduction> activeList = fdRepository.findAllByIsActive(true, Sort.by("id"));
        int count = 0;
        for (FixedDeduction fd : activeList) {
            BigDecimal amt = resolveConfiguredAmount(
                    fd.getFormulaEnabled(), fd.getFormula(), null, ctx,
                    "FD/" + fd.getCode());
            if (amt == null) continue;

            final BigDecimal finalAmt = amt;
            empFdRepository.findByEmployee_IdAndFixedDeduction_IdAndPayrollMonth(
                    emp.getId(), fd.getId(), payrollMonth)
                    .ifPresentOrElse(
                            existing -> {
                                existing.setAmount(finalAmt);
                                existing.setModifiedBy(user);
                                empFdRepository.save(existing);
                            },
                            () -> empFdRepository.save(EmployeeFixedDeduction.builder()
                                    .employee(emp)
                                    .fixedDeduction(fd)
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
    // Overtime — stores RATE; amount = rate × hours
    // ─────────────────────────────────────────────────────────────────────────

    private int loadOt(Employee emp, String payrollMonth, BigDecimal basicSalary,
                       int workingDays, Map<String, Object> ctx, Usr user) {
        List<Overtime> activeList = otRepository.findAllByIsActive(true, Sort.by("id"));
        int count = 0;

        for (Overtime ot : activeList) {
            BigDecimal rate;
            if (Boolean.TRUE.equals(ot.getFormulaEnabled())
                    && ot.getFormula() != null && !ot.getFormula().isBlank()) {
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
    // ─────────────────────────────────────────────────────────────────────────

    private int loadNp(Employee emp, String payrollMonth, BigDecimal basicSalary,
                       int workingDays, Map<String, Object> ctx, Usr user) {
        List<NopayDays> activeList = npRepository.findAllByIsActive(true, Sort.by("id"));
        int count = 0;

        for (NopayDays nd : activeList) {
            BigDecimal rate;
            if (Boolean.TRUE.equals(nd.getFormulaEnabled())
                    && nd.getFormula() != null && !nd.getFormula().isBlank()) {
                rate = formulaEngineService.evaluate(nd.getFormula(), ctx);
                log.debug("NP rate formula [{}] emp={} → {}", nd.getCode(), emp.getId(), rate);
            } else {
                // Default: basicSalary / workingDays
                rate = workingDays > 0
                        ? basicSalary.divide(BigDecimal.valueOf(workingDays), 6, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
            }

            final BigDecimal finalRate = orZero(rate);
            empNpRepository.findByEmployee_IdAndNopayDays_IdAndPayrollMonth(
                    emp.getId(), nd.getId(), payrollMonth)
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
                                    .nopayDays(nd)
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
    // Late Deduction — stores RATE; amount = rate × hours
    // ─────────────────────────────────────────────────────────────────────────

    private int loadLate(Employee emp, String payrollMonth, BigDecimal basicSalary,
                         Map<String, Object> ctx, Usr user) {
        LateDeductionConfig config = lateConfigRepository.findFirstByIsActiveTrueOrderByIdAsc()
                .orElse(null);
        if (config == null) {
            log.debug("No active LateDeductionConfig — skipping late load for emp={}", emp.getId());
            return 0;
        }

        BigDecimal rate;
        if (Boolean.TRUE.equals(config.getFormulaEnabled())
                && config.getFormula() != null && !config.getFormula().isBlank()) {
            rate = formulaEngineService.evaluate(config.getFormula(), ctx);
            log.debug("Late rate formula emp={} → {}", emp.getId(), rate);
        } else {
            // Default: basicSalary / (config.workingDays × config.workingHoursPerDay)
            int configWd   = config.getWorkingDays()        != null ? config.getWorkingDays()        : 26;
            int configHpd  = config.getWorkingHoursPerDay() != null ? config.getWorkingHoursPerDay() : 8;
            int denominator = configWd * configHpd;
            rate = denominator > 0
                    ? basicSalary.divide(BigDecimal.valueOf(denominator), 6, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
        }

        final BigDecimal finalRate = orZero(rate);
        final LateDeductionConfig finalConfig = config;
        empLateRepository.findByEmployee_IdAndPayrollMonth(emp.getId(), payrollMonth)
                .ifPresentOrElse(
                        existing -> {
                            BigDecimal hours = orZero(existing.getHours());
                            existing.setRate(finalRate);
                            existing.setLateConfig(finalConfig);
                            existing.setAmount(finalRate.multiply(hours).setScale(2, RoundingMode.HALF_UP));
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
                    bonus.getFormulaEnabled(), bonus.getFormula(), null, ctx,
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
     * Evaluates a formula (if enabled and non-blank) or falls back to a static amount.
     * Returns {@code null} when neither a formula nor a positive static amount is configured,
     * signalling that this component should be skipped for this employee.
     */
    private BigDecimal resolveConfiguredAmount(Boolean formulaEnabled, String formula,
                                               BigDecimal staticAmount, Map<String, Object> ctx,
                                               String label) {
        if (Boolean.TRUE.equals(formulaEnabled) && formula != null && !formula.isBlank()) {
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
     * Falls back to 26 if no period record exists yet (e.g. period not yet created).
     */
    private int resolveWorkingDays(int month, int year) {
        return payrollPeriodRepository
                .findFirstByPeriodYearAndPeriodMonth(year, month)
                .map(PayrollPeriod::getWorkingDays)
                .filter(wd -> wd != null && wd > 0)
                .orElseGet(() -> {
                    log.warn("No PayrollPeriod found for {}-{} — defaulting workingDays to 26", year, String.format("%02d", month));
                    return 26;
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
