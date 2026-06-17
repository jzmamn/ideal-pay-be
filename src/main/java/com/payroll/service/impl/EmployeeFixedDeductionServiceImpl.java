package com.payroll.service.impl;

import com.payroll.config.SecurityContextHelper;
import com.payroll.dto.request.EmployeeFixedDeductionAssignRequestDTO;
import com.payroll.dto.request.EmployeeFixedDeductionRequestDTO;
import com.payroll.dto.response.EmployeeFixedDeductionResponseDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.entity.Employee;
import com.payroll.entity.EmployeeFixedDeduction;
import com.payroll.entity.FixedDeduction;
import com.payroll.entity.PayrollPeriod;
import com.payroll.entity.Usr;
import com.payroll.enums.PayrollRunStatus;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.formula.PayrollContextBuilder;
import com.payroll.mapper.EmployeeFixedDeductionMapper;
import com.payroll.repository.EmpPayrollRunRepository;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.EmployeeFixedDeductionRepository;
import com.payroll.repository.FixedDeductionRepository;
import com.payroll.repository.PayrollPeriodRepository;
import com.payroll.service.EmployeeFixedDeductionService;
import com.payroll.service.FormulaEngineService;
import com.payroll.service.PayrollPeriodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeFixedDeductionServiceImpl implements EmployeeFixedDeductionService {

    private final EmployeeFixedDeductionRepository employeeFixedDeductionRepository;
    private final EmployeeFixedDeductionMapper employeeFixedDeductionMapper;
    private final EmployeeRepository employeeRepository;
    private final FixedDeductionRepository fixedDeductionRepository;
    private final PayrollPeriodService payrollPeriodService;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final EmpPayrollRunRepository empPayrollRunRepository;
    private final SecurityContextHelper securityContextHelper;
    private final FormulaEngineService formulaEngineService;

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeFixedDeductionResponseDTO> getAllEmployeeFixedDeductions(boolean showDefaultRow) {
        Sort sort = Sort.by("id").ascending();
        return employeeFixedDeductionRepository.findAll(sort).stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(employeeFixedDeductionMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeFixedDeductionResponseDTO getEmployeeFixedDeductionById(Long id) {
        EmployeeFixedDeduction entity = employeeFixedDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeFixedDeduction", "id", id));
        return employeeFixedDeductionMapper.toResponseDTO(entity);
    }

    @Override
    public EmployeeFixedDeductionResponseDTO createEmployeeFixedDeduction(EmployeeFixedDeductionRequestDTO requestDTO) {
        EmployeeFixedDeduction entity = employeeFixedDeductionRepository
                .findByEmployee_IdAndFixedDeduction_IdAndPayrollMonth(
                        requestDTO.getEmpId(), requestDTO.getFdId(), requestDTO.getPayrollMonth())
                .orElse(null);

        if (entity != null) {
            entity.setAmount(requestDTO.getAmount());
            entity.setIsProcessed(requestDTO.getIsProcessed() != null ? requestDTO.getIsProcessed() : Boolean.FALSE);
            entity.setProcessedDate(requestDTO.getProcessedDate());
            entity.setModifiedBy(securityContextHelper.getCurrentUser());
        } else {
            entity = employeeFixedDeductionMapper.toEntity(requestDTO);
            setRelationships(entity, requestDTO);
        }

        return employeeFixedDeductionMapper.toResponseDTO(employeeFixedDeductionRepository.save(entity));
    }

    @Override
    public EmployeeFixedDeductionResponseDTO updateEmployeeFixedDeduction(Long id, EmployeeFixedDeductionRequestDTO requestDTO) {
        EmployeeFixedDeduction existing = employeeFixedDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeFixedDeduction", "id", id));
        employeeFixedDeductionMapper.updateEntityFromDTO(requestDTO, existing);
        updateRelationships(existing, requestDTO);
        return employeeFixedDeductionMapper.toResponseDTO(employeeFixedDeductionRepository.save(existing));
    }

    @Override
    public void deleteEmployeeFixedDeduction(Long id) {
        EmployeeFixedDeduction entity = employeeFixedDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeFixedDeduction", "id", id));
        employeeFixedDeductionRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeFixedDeductionResponseDTO> getByEmployeeId(Long empId) {
        Sort sort = Sort.by("id").ascending();
        return employeeFixedDeductionRepository.findAllByEmployeeId(empId, sort).stream()
                .map(employeeFixedDeductionMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeFixedDeductionResponseDTO> getByEmployeeId(Long empId, String payrollMonth) {
        return employeeFixedDeductionRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth).stream()
                .map(employeeFixedDeductionMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<EmployeeFixedDeductionResponseDTO> assignFixedDeductions(
            Long empId, EmployeeFixedDeductionAssignRequestDTO requestDTO) {

        String payrollMonth = requestDTO.getPayrollMonth();

        if (!payrollPeriodService.isPeriodOpen(payrollMonth)) {
            throw new IllegalStateException(
                    "Cannot modify fixed deductions — payroll period " + payrollMonth
                    + " is closed. Use a correction run instead.");
        }
        if (empPayrollRunRepository.existsByEmployee_IdAndPayrollMonthAndStatus(
                empId, payrollMonth, PayrollRunStatus.LOCKED)) {
            throw new IllegalStateException(
                    "Cannot modify fixed deductions — payroll is already locked for month: " + payrollMonth
                    + ". Use a correction run instead.");
        }

        List<EmployeeFixedDeduction> existing =
                employeeFixedDeductionRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth);
        Map<Long, EmployeeFixedDeduction> existingByFdId = existing.stream()
                .collect(Collectors.toMap(e -> e.getFixedDeduction().getId(), e -> e, (a, b) -> b));

        Set<Long> selectedFdIds = requestDTO.getSelections().stream()
                .map(EmployeeFixedDeductionAssignRequestDTO.Selection::getFdId)
                .collect(Collectors.toSet());

        // Unselected deductions: remove any existing assignment for this employee/month.
        List<EmployeeFixedDeduction> toRemove = existing.stream()
                .filter(e -> !selectedFdIds.contains(e.getFixedDeduction().getId()))
                .toList();
        if (!toRemove.isEmpty()) {
            employeeFixedDeductionRepository.deleteAll(toRemove);
        }

        Usr currentUser = securityContextHelper.getCurrentUser();

        for (EmployeeFixedDeductionAssignRequestDTO.Selection selection : requestDTO.getSelections()) {
            EmployeeFixedDeduction entity = existingByFdId.get(selection.getFdId());
            if (entity != null) {
                entity.setAmount(selection.getAmount());
                entity.setModifiedBy(currentUser);
                employeeFixedDeductionRepository.save(entity);
            } else {
                employeeFixedDeductionRepository.save(EmployeeFixedDeduction.builder()
                        .employee(employeeRepository.getReferenceById(empId))
                        .fixedDeduction(fixedDeductionRepository.getReferenceById(selection.getFdId()))
                        .amount(selection.getAmount())
                        .payrollMonth(payrollMonth)
                        .isProcessed(false)
                        .formulaCalculated(false)
                        .createdBy(currentUser)
                        .modifiedBy(currentUser)
                        .build());
            }
        }

        return getByEmployeeId(empId, payrollMonth);
    }

    @Override
    @Transactional(readOnly = true)
    public FormulaEvaluateResponseDTO previewAmount(Long empId, Long fdId, String payrollMonth) {
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", empId));
        FixedDeduction fixedDeduction = fixedDeductionRepository.findById(fdId)
                .orElseThrow(() -> new ResourceNotFoundException("FixedDeduction", "id", fdId));

        int workingDays = resolveWorkingDays(payrollMonth);
        Map<String, Object> ctx = PayrollContextBuilder.builder()
                .employee(employee)
                .workingDays(workingDays)
                .build();

        String formula = fixedDeduction.getFormula();
        if (formula != null && !formula.isBlank()) {
            try {
                BigDecimal result = formulaEngineService.evaluate(formula, ctx);
                return FormulaEvaluateResponseDTO.builder()
                        .expression(formula)
                        .result(result != null ? result : BigDecimal.ZERO)
                        .context(sanitise(ctx))
                        .build();
            } catch (Exception ex) {
                log.warn("Formula evaluation failed for FD [{}] emp={}: {}",
                        fixedDeduction.getCode(), empId, ex.getMessage());
                return FormulaEvaluateResponseDTO.builder()
                        .expression(formula)
                        .context(sanitise(ctx))
                        .technicalError(ex.getMessage())
                        .userFriendlyError(formulaEngineService.toUserFriendlyMessage(ex))
                        .build();
            }
        }

        // Fixed Deductions have no static fallback amount — no formula means zero.
        return FormulaEvaluateResponseDTO.builder()
                .expression("no formula configured")
                .result(BigDecimal.ZERO)
                .context(sanitise(ctx))
                .build();
    }

    /**
     * Looks up {@code PayrollPeriod.workingDays} for the given {@code YYYY-MM} payroll month.
     * Falls back to 26 when the month is missing/unparsable or no period record exists yet.
     */
    private int resolveWorkingDays(String payrollMonth) {
        if (payrollMonth == null || payrollMonth.isBlank()) return 26;
        try {
            String[] parts = payrollMonth.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            return payrollPeriodRepository.findFirstByPeriodYearAndPeriodMonth(year, month)
                    .map(PayrollPeriod::getWorkingDays)
                    .filter(wd -> wd != null && wd > 0)
                    .orElse(26);
        } catch (Exception ex) {
            log.warn("Could not resolve working days for payrollMonth='{}' — defaulting to 26", payrollMonth);
            return 26;
        }
    }

    private Map<String, Object> sanitise(Map<String, Object> ctx) {
        Map<String, Object> safe = new HashMap<>();
        ctx.forEach((k, v) -> {
            if (v instanceof Number || v instanceof String || v instanceof Boolean || v == null) {
                safe.put(k, v);
            } else {
                safe.put(k, v.toString());
            }
        });
        return safe;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setRelationships(EmployeeFixedDeduction entity, EmployeeFixedDeductionRequestDTO dto) {
        entity.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        entity.setFixedDeduction(fixedDeductionRepository.getReferenceById(dto.getFdId()));
        var currentUser = securityContextHelper.getCurrentUser();
        entity.setCreatedBy(currentUser);
        entity.setModifiedBy(currentUser);
    }

    private void updateRelationships(EmployeeFixedDeduction entity, EmployeeFixedDeductionRequestDTO dto) {
        if (dto.getEmpId() != null)
            entity.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        if (dto.getFdId() != null)
            entity.setFixedDeduction(fixedDeductionRepository.getReferenceById(dto.getFdId()));
        entity.setModifiedBy(securityContextHelper.getCurrentUser());
    }
}
