package com.payroll.service.impl;

import com.payroll.config.SecurityContextHelper;
import com.payroll.dto.request.EmployeeFixedAllowanceAssignRequestDTO;
import com.payroll.dto.request.EmployeeFixedAllowanceRequestDTO;
import com.payroll.dto.response.EmployeeFixedAllowanceResponseDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.entity.Employee;
import com.payroll.entity.EmployeeFixedAllowance;
import com.payroll.entity.FixedAllowance;
import com.payroll.entity.PayrollPeriod;
import com.payroll.entity.Usr;
import com.payroll.enums.PayrollRunStatus;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.formula.PayrollContextBuilder;
import com.payroll.mapper.EmployeeFixedAllowanceMapper;
import com.payroll.repository.EmpPayrollRunRepository;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.EmployeeFixedAllowanceRepository;
import com.payroll.repository.FixedAllowanceRepository;
import com.payroll.repository.PayrollPeriodRepository;
import com.payroll.service.EmployeeFixedAllowanceService;
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
public class EmployeeFixedAllowanceServiceImpl implements EmployeeFixedAllowanceService {

    private final EmployeeFixedAllowanceRepository employeeFixedAllowanceRepository;
    private final EmployeeFixedAllowanceMapper employeeFixedAllowanceMapper;
    private final EmployeeRepository employeeRepository;
    private final FixedAllowanceRepository fixedAllowanceRepository;
    private final PayrollPeriodService payrollPeriodService;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final EmpPayrollRunRepository empPayrollRunRepository;
    private final SecurityContextHelper securityContextHelper;
    private final FormulaEngineService formulaEngineService;

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeFixedAllowanceResponseDTO> getAllEmployeeFixedAllowances(boolean showDefaultRow) {
        Sort sort = Sort.by("id").ascending();
        return employeeFixedAllowanceRepository.findAll(sort).stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(employeeFixedAllowanceMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeFixedAllowanceResponseDTO getEmployeeFixedAllowanceById(Long id) {
        EmployeeFixedAllowance entity = employeeFixedAllowanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeFixedAllowance", "id", id));
        return employeeFixedAllowanceMapper.toResponseDTO(entity);
    }

    @Override
    public EmployeeFixedAllowanceResponseDTO createEmployeeFixedAllowance(EmployeeFixedAllowanceRequestDTO requestDTO) {
        EmployeeFixedAllowance entity = employeeFixedAllowanceRepository
                .findByEmployee_IdAndFixedAllowance_IdAndPayrollMonth(
                        requestDTO.getEmpId(), requestDTO.getFaId(), requestDTO.getPayrollMonth())
                .orElse(null);

        if (entity != null) {
            entity.setAmount(requestDTO.getAmount());
            entity.setIsProcessed(requestDTO.getIsProcessed() != null ? requestDTO.getIsProcessed() : Boolean.FALSE);
            entity.setProcessedDate(requestDTO.getProcessedDate());
            entity.setModifiedBy(securityContextHelper.getCurrentUser());
        } else {
            entity = employeeFixedAllowanceMapper.toEntity(requestDTO);
            setRelationships(entity, requestDTO);
        }

        return employeeFixedAllowanceMapper.toResponseDTO(employeeFixedAllowanceRepository.save(entity));
    }

    @Override
    public EmployeeFixedAllowanceResponseDTO updateEmployeeFixedAllowance(Long id, EmployeeFixedAllowanceRequestDTO requestDTO) {
        EmployeeFixedAllowance existing = employeeFixedAllowanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeFixedAllowance", "id", id));
        employeeFixedAllowanceMapper.updateEntityFromDTO(requestDTO, existing);
        updateRelationships(existing, requestDTO);
        return employeeFixedAllowanceMapper.toResponseDTO(employeeFixedAllowanceRepository.save(existing));
    }

    @Override
    public void deleteEmployeeFixedAllowance(Long id) {
        EmployeeFixedAllowance entity = employeeFixedAllowanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeFixedAllowance", "id", id));
        employeeFixedAllowanceRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeFixedAllowanceResponseDTO> getByEmployeeId(Long empId) {
        Sort sort = Sort.by("id").ascending();
        return employeeFixedAllowanceRepository.findAllByEmployeeId(empId, sort).stream()
                .map(employeeFixedAllowanceMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeFixedAllowanceResponseDTO> getByEmployeeId(Long empId, String payrollMonth) {
        return employeeFixedAllowanceRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth).stream()
                .map(employeeFixedAllowanceMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<EmployeeFixedAllowanceResponseDTO> assignFixedAllowances(
            Long empId, EmployeeFixedAllowanceAssignRequestDTO requestDTO) {

        String payrollMonth = requestDTO.getPayrollMonth();

        if (!payrollPeriodService.isPeriodOpen(payrollMonth)) {
            throw new IllegalStateException(
                    "Cannot modify fixed allowances — payroll period " + payrollMonth
                    + " is closed. Use a correction run instead.");
        }
        if (empPayrollRunRepository.existsByEmployee_IdAndPayrollMonthAndStatus(
                empId, payrollMonth, PayrollRunStatus.LOCKED)) {
            throw new IllegalStateException(
                    "Cannot modify fixed allowances — payroll is already locked for month: " + payrollMonth
                    + ". Use a correction run instead.");
        }

        List<EmployeeFixedAllowance> existing =
                employeeFixedAllowanceRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth);
        Map<Long, EmployeeFixedAllowance> existingByFaId = existing.stream()
                .collect(Collectors.toMap(e -> e.getFixedAllowance().getId(), e -> e, (a, b) -> b));

        Set<Long> selectedFaIds = requestDTO.getSelections().stream()
                .map(EmployeeFixedAllowanceAssignRequestDTO.Selection::getFaId)
                .collect(Collectors.toSet());

        // Unselected allowances: remove any existing assignment for this employee/month.
        List<EmployeeFixedAllowance> toRemove = existing.stream()
                .filter(e -> !selectedFaIds.contains(e.getFixedAllowance().getId()))
                .toList();
        if (!toRemove.isEmpty()) {
            employeeFixedAllowanceRepository.deleteAll(toRemove);
        }

        Usr currentUser = securityContextHelper.getCurrentUser();

        for (EmployeeFixedAllowanceAssignRequestDTO.Selection selection : requestDTO.getSelections()) {
            EmployeeFixedAllowance entity = existingByFaId.get(selection.getFaId());
            if (entity != null) {
                entity.setAmount(selection.getAmount());
                entity.setModifiedBy(currentUser);
                employeeFixedAllowanceRepository.save(entity);
            } else {
                employeeFixedAllowanceRepository.save(EmployeeFixedAllowance.builder()
                        .employee(employeeRepository.getReferenceById(empId))
                        .fixedAllowance(fixedAllowanceRepository.getReferenceById(selection.getFaId()))
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
    public FormulaEvaluateResponseDTO previewAmount(Long empId, Long faId, String payrollMonth) {
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", empId));
        FixedAllowance fixedAllowance = fixedAllowanceRepository.findById(faId)
                .orElseThrow(() -> new ResourceNotFoundException("FixedAllowance", "id", faId));

        int workingDays = resolveWorkingDays(payrollMonth);
        Map<String, Object> ctx = PayrollContextBuilder.builder()
                .employee(employee)
                .workingDays(workingDays)
                .build();

        String formula = fixedAllowance.getFormula();
        if (formula != null && !formula.isBlank()) {
            try {
                BigDecimal result = formulaEngineService.evaluate(formula, ctx);
                return FormulaEvaluateResponseDTO.builder()
                        .expression(formula)
                        .result(result != null ? result : BigDecimal.ZERO)
                        .context(sanitise(ctx))
                        .build();
            } catch (Exception ex) {
                log.warn("Formula evaluation failed for FA [{}] emp={}: {}",
                        fixedAllowance.getCode(), empId, ex.getMessage());
                return FormulaEvaluateResponseDTO.builder()
                        .expression(formula)
                        .context(sanitise(ctx))
                        .technicalError(ex.getMessage())
                        .userFriendlyError(formulaEngineService.toUserFriendlyMessage(ex))
                        .build();
            }
        }

        // No formula configured — fall back to the FixedAllowance's static company-level amount.
        BigDecimal staticAmount = fixedAllowance.getAmount() != null ? fixedAllowance.getAmount() : BigDecimal.ZERO;
        return FormulaEvaluateResponseDTO.builder()
                .expression("no formula configured — using fixed amount")
                .result(staticAmount)
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

    private void setRelationships(EmployeeFixedAllowance entity, EmployeeFixedAllowanceRequestDTO dto) {
        entity.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        entity.setFixedAllowance(fixedAllowanceRepository.getReferenceById(dto.getFaId()));
        var currentUser = securityContextHelper.getCurrentUser();
        entity.setCreatedBy(currentUser);
        entity.setModifiedBy(currentUser);
    }

    private void updateRelationships(EmployeeFixedAllowance entity, EmployeeFixedAllowanceRequestDTO dto) {
        if (dto.getEmpId() != null)
            entity.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        if (dto.getFaId() != null)
            entity.setFixedAllowance(fixedAllowanceRepository.getReferenceById(dto.getFaId()));
        entity.setModifiedBy(securityContextHelper.getCurrentUser());
    }
}
