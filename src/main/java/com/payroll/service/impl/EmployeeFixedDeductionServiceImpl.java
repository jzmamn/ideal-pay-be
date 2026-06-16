package com.payroll.service.impl;

import com.payroll.dto.request.EmployeeFixedDeductionAssignRequestDTO;
import com.payroll.dto.request.EmployeeFixedDeductionRequestDTO;
import com.payroll.dto.response.EmployeeFixedDeductionResponseDTO;
import com.payroll.entity.EmployeeFixedDeduction;
import com.payroll.entity.Usr;
import com.payroll.enums.PayrollRunStatus;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.EmployeeFixedDeductionMapper;
import com.payroll.repository.EmpPayrollRunRepository;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.EmployeeFixedDeductionRepository;
import com.payroll.repository.FixedDeductionRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.EmployeeFixedDeductionService;
import com.payroll.service.PayrollPeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeFixedDeductionServiceImpl implements EmployeeFixedDeductionService {

    private final EmployeeFixedDeductionRepository employeeFixedDeductionRepository;
    private final EmployeeFixedDeductionMapper employeeFixedDeductionMapper;
    private final EmployeeRepository employeeRepository;
    private final FixedDeductionRepository fixedDeductionRepository;
    private final UsrRepository usrRepository;
    private final PayrollPeriodService payrollPeriodService;
    private final EmpPayrollRunRepository empPayrollRunRepository;

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
            entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
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

        Usr createdByUser  = usrRepository.getReferenceById(requestDTO.getCreatedBy());
        Usr modifiedByUser = usrRepository.getReferenceById(requestDTO.getModifiedBy());

        for (EmployeeFixedDeductionAssignRequestDTO.Selection selection : requestDTO.getSelections()) {
            EmployeeFixedDeduction entity = existingByFdId.get(selection.getFdId());
            if (entity != null) {
                entity.setAmount(selection.getAmount());
                entity.setModifiedBy(modifiedByUser);
                employeeFixedDeductionRepository.save(entity);
            } else {
                employeeFixedDeductionRepository.save(EmployeeFixedDeduction.builder()
                        .employee(employeeRepository.getReferenceById(empId))
                        .fixedDeduction(fixedDeductionRepository.getReferenceById(selection.getFdId()))
                        .amount(selection.getAmount())
                        .payrollMonth(payrollMonth)
                        .isProcessed(false)
                        .formulaCalculated(false)
                        .createdBy(createdByUser)
                        .modifiedBy(modifiedByUser)
                        .build());
            }
        }

        return getByEmployeeId(empId, payrollMonth);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setRelationships(EmployeeFixedDeduction entity, EmployeeFixedDeductionRequestDTO dto) {
        entity.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        entity.setFixedDeduction(fixedDeductionRepository.getReferenceById(dto.getFdId()));
        entity.setCreatedBy(usrRepository.getReferenceById(dto.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }

    private void updateRelationships(EmployeeFixedDeduction entity, EmployeeFixedDeductionRequestDTO dto) {
        if (dto.getEmpId() != null)
            entity.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        if (dto.getFdId() != null)
            entity.setFixedDeduction(fixedDeductionRepository.getReferenceById(dto.getFdId()));
        if (dto.getModifiedBy() != null)
            entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }
}
