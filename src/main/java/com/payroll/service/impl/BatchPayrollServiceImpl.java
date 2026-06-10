package com.payroll.service.impl;

import com.payroll.dto.request.BatchSaveEntryDTO;
import com.payroll.dto.request.BatchSaveRequestDTO;
import com.payroll.entity.*;
import com.payroll.entity.EmployeeSalaryAdvance;
import com.payroll.entity.EmployeeBonus;
import com.payroll.entity.EmployeeLoan;
import com.payroll.entity.EmployeeSalaryIncrement;
import com.payroll.repository.EmployeeSalaryAdvanceRepository;
import com.payroll.repository.EmployeeBonusRepository;
import com.payroll.repository.EmployeeLoanRepository;
import com.payroll.repository.EmployeeSalaryIncrementRepository;
import com.payroll.repository.LoanRepository;
import com.payroll.enums.PayrollRunStatus;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.repository.*;
import com.payroll.service.BatchPayrollService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BatchPayrollServiceImpl implements BatchPayrollService {

    private final JdbcTemplate jdbcTemplate;

    // Master component repositories — code → entity resolution
    private final FixedAllowanceRepository    faRepository;
    private final FixedDeductionRepository    fdRepository;
    private final VariableAllowanceRepository vaRepository;
    private final VariableDeductionRepository vdRepository;
    private final OvertimeRepository          otRepository;
    private final NopayDaysRepository         npRepository;
    private final LoanRepository              loanRepository;

    // Employee component repositories — upsert targets
    private final EmployeeFixedAllowanceRepository    empFaRepository;
    private final EmployeeFixedDeductionRepository    empFdRepository;
    private final EmployeeVariableAllowanceRepository empVaRepository;
    private final EmployeeVariableDeductionRepository empVdRepository;
    private final EmployeeOvertimeRepository          empOtRepository;
    private final EmployeeNopayRepository             empNpRepository;
    private final EmployeeLateRepository              empLateRepository;
    private final EmployeeSalaryAdvanceRepository     empSalAdvRepository;
    private final EmployeeBonusRepository             empBonusRepository;
    private final BonusRepository                     bonusRepository;
    private final EmployeeLoanRepository              empLoanRepository;
    private final EmployeeSalaryIncrementRepository   empSalIncrRepository;

    private final EmployeeRepository      employeeRepository;
    private final UsrRepository           usrRepository;
    private final EmpPayrollRunRepository payrollRunRepository;

    // ── Load ─────────────────────────────────────────────────────────────────

    @Override
    public Map<String, List<Map<String, Object>>> load(Integer periodMonth, Integer periodYear) {
        String payrollMonth = toPayrollMonth(periodMonth, periodYear);
        log.debug("Loading batch pivot data for month: {}", payrollMonth);

        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        result.put("fixedAllowances",    callSp("sp_emp_fa_pivot", payrollMonth));
        result.put("fixedDeductions",    callSp("sp_emp_fd_pivot", payrollMonth));
        result.put("variableAllowances", callSp("sp_emp_va_pivot", payrollMonth));
        result.put("variableDeductions", callSp("sp_emp_vd_pivot", payrollMonth));
        result.put("overtimes",          callSp("sp_emp_ot_pivot", payrollMonth));
        result.put("nopays",             callSp("sp_emp_np_pivot",      payrollMonth));
        result.put("lates",              callSp("sp_emp_late_pivot",    payrollMonth));
        result.put("salaryAdvances",     callSp("sp_emp_sal_adv_pivot", payrollMonth));
        result.put("bonuses",            callSp("sp_emp_bonus_pivot",   payrollMonth));
        result.put("loans",              callSp("sp_emp_loan_pivot",    payrollMonth));
        result.put("salaryIncrements",   callSp("sp_emp_sal_incr_pivot",payrollMonth));
        return result;
    }

    // ── Save ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void save(BatchSaveRequestDTO requestDTO, Long modifiedBy) {
        String payrollMonth = toPayrollMonth(requestDTO.getPeriodMonth(), requestDTO.getPeriodYear());
        log.debug("Saving batch entries for month: {}", payrollMonth);

        Usr user = usrRepository.findById(modifiedBy)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", modifiedBy));

        // Separate entries into upsert (amount > 0) and delete (amount == 0 or null)
        List<BatchSaveEntryDTO> allEntries = requestDTO.getEntries().stream()
                .filter(e -> e.getComponentCode() != null
                        && e.getComponentType() != null
                        && e.getEmployeeId() != null)
                .toList();

        if (allEntries.isEmpty()) {
            log.debug("No entries to process for month: {}", payrollMonth);
            return;
        }

        // Guard — collect all employee IDs that have a LOCKED run for this month
        Set<Long> lockedEmpIds = new HashSet<>();
        for (BatchSaveEntryDTO entry : allEntries) {
            if (payrollRunRepository.existsByEmployee_IdAndPayrollMonthAndStatus(
                    entry.getEmployeeId(), payrollMonth, PayrollRunStatus.LOCKED)) {
                lockedEmpIds.add(entry.getEmployeeId());
            }
        }
        if (!lockedEmpIds.isEmpty()) {
            throw new IllegalStateException(
                    "Payroll already locked for employee IDs " + lockedEmpIds + " for month: " + payrollMonth);
        }

        // Process each entry — upsert if amount > 0, delete if amount == 0 or null
        for (BatchSaveEntryDTO entry : allEntries) {
            Employee employee = employeeRepository.findById(entry.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", entry.getEmployeeId()));

            boolean isDelete = entry.getAmount() == null
                    || entry.getAmount().compareTo(BigDecimal.ZERO) == 0;

            switch (entry.getComponentType().toUpperCase()) {
                case "FA"    -> { if (isDelete) deleteFa(employee, entry, payrollMonth); else upsertFa(employee, entry, payrollMonth, user); }
                case "FD"    -> { if (isDelete) deleteFd(employee, entry, payrollMonth); else upsertFd(employee, entry, payrollMonth, user); }
                case "VA"    -> { if (isDelete) deleteVa(employee, entry, payrollMonth); else upsertVa(employee, entry, payrollMonth, user); }
                case "VD"    -> upsertVd(employee, entry, payrollMonth, user);
                case "OT"    -> { if (isDelete) deleteOt(employee, entry, payrollMonth); else upsertOt(employee, entry, payrollMonth, user); }
                case "NOPAY"   -> { if (isDelete) deleteNp(employee, entry, payrollMonth);   else upsertNp(employee, entry, payrollMonth, user); }
                case "LATE"    -> { if (isDelete) deleteLate(employee, payrollMonth);         else upsertLate(employee, entry, payrollMonth, user); }
                case "SAL_ADV"  -> { if (isDelete) deleteSalAdv(employee, payrollMonth);               else upsertSalAdv(employee, entry, payrollMonth, user); }
                case "BONUS"    -> { if (isDelete) deleteBonus(employee, entry, payrollMonth);           else upsertBonus(employee, entry, payrollMonth, user); }
                case "LOAN"     -> { if (isDelete) deleteLoan(employee, entry, payrollMonth);          else upsertLoan(employee, entry, payrollMonth, user); }
                case "SAL_INCR" -> { if (isDelete) deleteSalIncr(employee, payrollMonth);              else upsertSalIncr(employee, entry, payrollMonth, user); }
                default         -> log.warn("Skipping unknown componentType '{}' for employee {}",
                        entry.getComponentType(), entry.getEmployeeId());
            }
        }
    }

    // ── Delete helpers ───────────────────────────────────────────────────────

    private void deleteFa(Employee emp, BatchSaveEntryDTO entry, String payrollMonth) {
        faRepository.findByCodeIgnoreCase(entry.getComponentCode()).ifPresent(fa ->
            empFaRepository.findAllByEmployeeIdAndPayrollMonth(emp.getId(), payrollMonth).stream()
                .filter(r -> r.getFixedAllowance().getId().equals(fa.getId()))
                .findFirst()
                .ifPresent(empFaRepository::delete));
    }

    private void deleteFd(Employee emp, BatchSaveEntryDTO entry, String payrollMonth) {
        fdRepository.findByCodeIgnoreCase(entry.getComponentCode()).ifPresent(fd ->
            empFdRepository.findAllByEmployeeIdAndPayrollMonth(emp.getId(), payrollMonth).stream()
                .filter(r -> r.getFixedDeduction().getId().equals(fd.getId()))
                .findFirst()
                .ifPresent(empFdRepository::delete));
    }

    private void deleteVa(Employee emp, BatchSaveEntryDTO entry, String payrollMonth) {
        vaRepository.findByCodeIgnoreCase(entry.getComponentCode()).ifPresent(va ->
            empVaRepository.findAllByEmployeeIdAndPayrollMonth(emp.getId(), payrollMonth).stream()
                .filter(r -> r.getVariableAllowance().getId().equals(va.getId()))
                .findFirst()
                .ifPresent(empVaRepository::delete));
    }

    private void deleteVd(Employee emp, BatchSaveEntryDTO entry, String payrollMonth) {
        vdRepository.findByCodeIgnoreCase(entry.getComponentCode()).ifPresent(vd ->
            empVdRepository.findAllByEmployeeIdAndPayrollMonth(emp.getId(), payrollMonth).stream()
                .filter(r -> r.getVariableDeduction().getId().equals(vd.getId()))
                .findFirst()
                .ifPresent(empVdRepository::delete));
    }

    private void deleteOt(Employee emp, BatchSaveEntryDTO entry, String payrollMonth) {
        otRepository.findByCodeIgnoreCase(entry.getComponentCode()).ifPresent(ot ->
            empOtRepository.findAllByEmployeeIdAndPayrollMonth(emp.getId(), payrollMonth).stream()
                .filter(r -> r.getOvertime().getId().equals(ot.getId()))
                .findFirst()
                .ifPresent(empOtRepository::delete));
    }

    private void deleteNp(Employee emp, BatchSaveEntryDTO entry, String payrollMonth) {
        npRepository.findByCodeIgnoreCase(entry.getComponentCode()).ifPresent(np ->
            empNpRepository.findAllByEmployeeIdAndPayrollMonth(emp.getId(), payrollMonth).stream()
                .filter(r -> r.getNopayDays().getId().equals(np.getId()))
                .findFirst()
                .ifPresent(empNpRepository::delete));
    }

    private void deleteLate(Employee emp, String payrollMonth) {
        empLateRepository.findByEmployee_IdAndPayrollMonth(emp.getId(), payrollMonth)
                .ifPresent(empLateRepository::delete);
    }

    // ── SP caller ─────────────────────────────────────────────────────────────

    private List<Map<String, Object>> callSp(String spName, String payrollMonth) {
        try {
            return jdbcTemplate.queryForList("CALL " + spName + "(?)", payrollMonth);
        } catch (Exception ex) {
            log.error("SP {} failed for month {}: {}", spName, payrollMonth, ex.getMessage());
            return Collections.emptyList();
        }
    }

    // ── Upsert — Fixed Allowance ──────────────────────────────────────────────

    private void upsertFa(Employee emp, BatchSaveEntryDTO entry, String payrollMonth, Usr user) {
        FixedAllowance fa = faRepository.findByCodeIgnoreCase(entry.getComponentCode())
                .orElseThrow(() -> new ResourceNotFoundException("FixedAllowance", "code", entry.getComponentCode()));

        empFaRepository.findAllByEmployeeIdAndPayrollMonth(emp.getId(), payrollMonth)
                .stream()
                .filter(r -> r.getFixedAllowance().getId().equals(fa.getId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> {
                            existing.setAmount(entry.getAmount());
                            existing.setModifiedBy(user);
                            empFaRepository.save(existing);
                        },
                        () -> empFaRepository.save(EmployeeFixedAllowance.builder()
                                .employee(emp)
                                .fixedAllowance(fa)
                                .amount(entry.getAmount())
                                .payrollMonth(payrollMonth)
                                .isProcessed(false)
                                .createdBy(user)
                                .modifiedBy(user)
                                .build())
                );
    }

    // ── Upsert — Fixed Deduction ──────────────────────────────────────────────

    private void upsertFd(Employee emp, BatchSaveEntryDTO entry, String payrollMonth, Usr user) {
        FixedDeduction fd = fdRepository.findByCodeIgnoreCase(entry.getComponentCode())
                .orElseThrow(() -> new ResourceNotFoundException("FixedDeduction", "code", entry.getComponentCode()));

        empFdRepository.findAllByEmployeeIdAndPayrollMonth(emp.getId(), payrollMonth)
                .stream()
                .filter(r -> r.getFixedDeduction().getId().equals(fd.getId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> {
                            existing.setAmount(entry.getAmount());
                            existing.setModifiedBy(user);
                            empFdRepository.save(existing);
                        },
                        () -> empFdRepository.save(EmployeeFixedDeduction.builder()
                                .employee(emp)
                                .fixedDeduction(fd)
                                .amount(entry.getAmount())
                                .payrollMonth(payrollMonth)
                                .isProcessed(false)
                                .createdBy(user)
                                .modifiedBy(user)
                                .build())
                );
    }

    // ── Upsert — Variable Allowance ───────────────────────────────────────────

    private void upsertVa(Employee emp, BatchSaveEntryDTO entry, String payrollMonth, Usr user) {
        VariableAllowance va = vaRepository.findByCodeIgnoreCase(entry.getComponentCode())
                .orElseThrow(() -> new ResourceNotFoundException("VariableAllowance", "code", entry.getComponentCode()));

        empVaRepository.findAllByEmployeeIdAndPayrollMonth(emp.getId(), payrollMonth)
                .stream()
                .filter(r -> r.getVariableAllowance().getId().equals(va.getId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> {
                            existing.setAmount(entry.getAmount());
                            existing.setModifiedBy(user);
                            empVaRepository.save(existing);
                        },
                        () -> empVaRepository.save(EmployeeVariableAllowance.builder()
                                .employee(emp)
                                .variableAllowance(va)
                                .amount(entry.getAmount())
                                .payrollMonth(payrollMonth)
                                .isProcessed(false)
                                .createdBy(user)
                                .modifiedBy(user)
                                .build())
                );
    }

    // ── Upsert — Variable Deduction ───────────────────────────────────────────

    private void upsertVd(Employee emp, BatchSaveEntryDTO entry, String payrollMonth, Usr user) {
        VariableDeduction vd = vdRepository.findByCodeIgnoreCase(entry.getComponentCode())
                .orElseThrow(() -> new ResourceNotFoundException("VariableDeduction", "code", entry.getComponentCode()));

        empVdRepository.findAllByEmployeeIdAndPayrollMonth(emp.getId(), payrollMonth)
                .stream()
                .filter(r -> r.getVariableDeduction().getId().equals(vd.getId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> {
                            existing.setAmount(entry.getAmount());
                            existing.setModifiedBy(user);
                            empVdRepository.save(existing);
                        },
                        () -> empVdRepository.save(EmployeeVariableDeduction.builder()
                                .employee(emp)
                                .variableDeduction(vd)
                                .amount(entry.getAmount())
                                .payrollMonth(payrollMonth)
                                .isProcessed(false)
                                .createdBy(user)
                                .modifiedBy(user)
                                .build())
                );
    }

    // ── Upsert — Overtime ─────────────────────────────────────────────────────

    private void upsertOt(Employee emp, BatchSaveEntryDTO entry, String payrollMonth, Usr user) {
        Overtime ot = otRepository.findByCodeIgnoreCase(entry.getComponentCode())
                .orElseThrow(() -> new ResourceNotFoundException("Overtime", "code", entry.getComponentCode()));

        BigDecimal hours = entry.getHours() != null ? entry.getHours() : BigDecimal.ZERO;

        empOtRepository.findAllByEmployeeIdAndPayrollMonth(emp.getId(), payrollMonth)
                .stream()
                .filter(r -> r.getOvertime().getId().equals(ot.getId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> {
                            existing.setHours(hours);
                            existing.setAmount(entry.getAmount());
                            existing.setModifiedBy(user);
                            empOtRepository.save(existing);
                        },
                        () -> empOtRepository.save(EmployeeOvertime.builder()
                                .employee(emp)
                                .overtime(ot)
                                .hours(hours)
                                .amount(entry.getAmount())
                                .payrollMonth(payrollMonth)
                                .isProcessed(false)
                                .createdBy(user)
                                .modifiedBy(user)
                                .build())
                );
    }

    // ── Upsert — NoPay ────────────────────────────────────────────────────────

    private void upsertNp(Employee emp, BatchSaveEntryDTO entry, String payrollMonth, Usr user) {
        NopayDays np = npRepository.findByCodeIgnoreCase(entry.getComponentCode())
                .orElseThrow(() -> new ResourceNotFoundException("NopayDays", "code", entry.getComponentCode()));

        BigDecimal days = entry.getDays() != null ? entry.getDays() : BigDecimal.ZERO;

        empNpRepository.findAllByEmployeeIdAndPayrollMonth(emp.getId(), payrollMonth)
                .stream()
                .filter(r -> r.getNopayDays().getId().equals(np.getId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> {
                            existing.setDays(days);
                            existing.setAmount(entry.getAmount());
                            existing.setModifiedBy(user);
                            empNpRepository.save(existing);
                        },
                        () -> empNpRepository.save(EmployeeNopay.builder()
                                .employee(emp)
                                .nopayDays(np)
                                .days(days)
                                .amount(entry.getAmount())
                                .payrollMonth(payrollMonth)
                                .isProcessed(false)
                                .createdBy(user)
                                .modifiedBy(user)
                                .build())
                );
    }

    // ── Upsert — Late Deduction ───────────────────────────────────────────────

    private void upsertLate(Employee emp, BatchSaveEntryDTO entry, String payrollMonth, Usr user) {
        BigDecimal hours = entry.getHours() != null ? entry.getHours() : BigDecimal.ZERO;

        empLateRepository.findByEmployee_IdAndPayrollMonth(emp.getId(), payrollMonth)
                .ifPresentOrElse(
                        existing -> {
                            existing.setHours(hours);
                            existing.setAmount(entry.getAmount() != null ? entry.getAmount() : BigDecimal.ZERO);
                            existing.setModifiedBy(user);
                            empLateRepository.save(existing);
                        },
                        () -> empLateRepository.save(EmployeeLate.builder()
                                .employee(emp)
                                .hours(hours)
                                .amount(entry.getAmount() != null ? entry.getAmount() : BigDecimal.ZERO)
                                .payrollMonth(payrollMonth)
                                .isProcessed(false)
                                .createdBy(user)
                                .modifiedBy(user)
                                .build())
                );
    }

    // ── Upsert — Bonus ────────────────────────────────────────────────────────

    private void upsertBonus(Employee emp, BatchSaveEntryDTO entry, String payrollMonth, Usr user) {
        Bonus bonus = bonusRepository.findByCodeIgnoreCase(entry.getComponentCode())
                .orElseThrow(() -> new ResourceNotFoundException("Bonus", "code", entry.getComponentCode()));

        empBonusRepository.findByEmployeeIdAndPayrollMonthAndBonusId(emp.getId(), payrollMonth, bonus.getId())
                .ifPresentOrElse(
                        existing -> { existing.setAmount(entry.getAmount()); existing.setModifiedBy(user); empBonusRepository.save(existing); },
                        () -> empBonusRepository.save(EmployeeBonus.builder()
                                .employee(emp).bonus(bonus).amount(entry.getAmount()).payrollMonth(payrollMonth)
                                .isProcessed(false).createdBy(user).modifiedBy(user).build())
                );
    }

    private void deleteBonus(Employee emp, BatchSaveEntryDTO entry, String payrollMonth) {
        bonusRepository.findByCodeIgnoreCase(entry.getComponentCode()).ifPresent(bonus ->
            empBonusRepository.findByEmployeeIdAndPayrollMonthAndBonusId(emp.getId(), payrollMonth, bonus.getId())
                    .ifPresent(empBonusRepository::delete)
        );
    }

    // ── Upsert — Loan ─────────────────────────────────────────────────────────

    private void upsertLoan(Employee emp, BatchSaveEntryDTO entry, String payrollMonth, Usr user) {
        Loan loan = loanRepository.findByCodeIgnoreCase(entry.getComponentCode())
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "code", entry.getComponentCode()));

        empLoanRepository.findAllByEmployeeIdAndPayrollMonth(emp.getId(), payrollMonth).stream()
                .filter(r -> r.getLoan().getId().equals(loan.getId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> { existing.setAmount(entry.getAmount()); existing.setModifiedBy(user); empLoanRepository.save(existing); },
                        () -> empLoanRepository.save(EmployeeLoan.builder()
                                .employee(emp).loan(loan).amount(entry.getAmount()).payrollMonth(payrollMonth)
                                .isProcessed(false).createdBy(user).modifiedBy(user).build())
                );
    }

    private void deleteLoan(Employee emp, BatchSaveEntryDTO entry, String payrollMonth) {
        loanRepository.findByCodeIgnoreCase(entry.getComponentCode()).ifPresent(loan ->
                empLoanRepository.findAllByEmployeeIdAndPayrollMonth(emp.getId(), payrollMonth).stream()
                        .filter(r -> r.getLoan().getId().equals(loan.getId()))
                        .findFirst()
                        .ifPresent(empLoanRepository::delete));
    }

    // ── Upsert — Salary Increment ─────────────────────────────────────────────

    private void upsertSalIncr(Employee emp, BatchSaveEntryDTO entry, String payrollMonth, Usr user) {
        empSalIncrRepository.findByEmployeeIdAndPayrollMonth(emp.getId(), payrollMonth)
                .ifPresentOrElse(
                        existing -> { existing.setAmount(entry.getAmount()); existing.setModifiedBy(user); empSalIncrRepository.save(existing); },
                        () -> empSalIncrRepository.save(EmployeeSalaryIncrement.builder()
                                .employee(emp).amount(entry.getAmount()).payrollMonth(payrollMonth)
                                .isProcessed(false).createdBy(user).modifiedBy(user).build())
                );
    }

    private void deleteSalIncr(Employee emp, String payrollMonth) {
        empSalIncrRepository.findByEmployeeIdAndPayrollMonth(emp.getId(), payrollMonth)
                .ifPresent(empSalIncrRepository::delete);
    }

    // ── Upsert — Salary Advance ───────────────────────────────────────────────

    private void upsertSalAdv(Employee emp, BatchSaveEntryDTO entry, String payrollMonth, Usr user) {
        empSalAdvRepository.findByEmployeeIdAndPayrollMonth(emp.getId(), payrollMonth)
                .ifPresentOrElse(
                        existing -> {
                            existing.setAmount(entry.getAmount());
                            existing.setModifiedBy(user);
                            empSalAdvRepository.save(existing);
                        },
                        () -> empSalAdvRepository.save(EmployeeSalaryAdvance.builder()
                                .employee(emp)
                                .amount(entry.getAmount())
                                .payrollMonth(payrollMonth)
                                .isProcessed(false)
                                .createdBy(user)
                                .modifiedBy(user)
                                .build())
                );
    }

    private void deleteSalAdv(Employee emp, String payrollMonth) {
        empSalAdvRepository.findByEmployeeIdAndPayrollMonth(emp.getId(), payrollMonth)
                .ifPresent(empSalAdvRepository::delete);
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    /** Converts 5, 2026 → "2026-05" */
    private String toPayrollMonth(Integer month, Integer year) {
        return String.format("%d-%02d", year, month);
    }
}
