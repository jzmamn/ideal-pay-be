package com.payroll.service.impl;

import com.payroll.dto.request.BatchSaveEntryDTO;
import com.payroll.dto.request.BatchSaveRequestDTO;
import com.payroll.entity.*;
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

    // Employee component repositories — upsert targets
    private final EmployeeFixedAllowanceRepository    empFaRepository;
    private final EmployeeFixedDeductionRepository    empFdRepository;
    private final EmployeeVariableAllowanceRepository empVaRepository;
    private final EmployeeVariableDeductionRepository empVdRepository;
    private final EmployeeOvertimeRepository          empOtRepository;
    private final EmployeeNopayRepository             empNpRepository;

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
        result.put("nopays",             callSp("sp_emp_np_pivot", payrollMonth));
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
                case "NOPAY" -> { if (isDelete) deleteNp(employee, entry, payrollMonth); else upsertNp(employee, entry, payrollMonth, user); }
                default      -> log.warn("Skipping unknown componentType '{}' for employee {}",
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

    // ── Utility ───────────────────────────────────────────────────────────────

    /** Converts 5, 2026 → "2026-05" */
    private String toPayrollMonth(Integer month, Integer year) {
        return String.format("%d-%02d", year, month);
    }
}
