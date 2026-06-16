package com.payroll.service.impl;

import com.payroll.dto.request.EmployeeFixedAllowanceAssignRequestDTO;
import com.payroll.dto.request.EmployeeFixedAllowanceRequestDTO;
import com.payroll.dto.response.EmployeeFixedAllowanceResponseDTO;
import com.payroll.entity.EmployeeFixedAllowance;
import com.payroll.entity.Usr;
import com.payroll.enums.PayrollRunStatus;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.EmployeeFixedAllowanceMapper;
import com.payroll.repository.EmpPayrollRunRepository;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.EmployeeFixedAllowanceRepository;
import com.payroll.repository.FixedAllowanceRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.EmployeeFixedAllowanceService;
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
public class EmployeeFixedAllowanceServiceImpl implements EmployeeFixedAllowanceService {

    private final EmployeeFixedAllowanceRepository employeeFixedAllowanceRepository;
    private final EmployeeFixedAllowanceMapper employeeFixedAllowanceMapper;
    private final EmployeeRepository employeeRepository;
    private final FixedAllowanceRepository fixedAllowanceRepository;
    private final UsrRepository usrRepository;
    private final PayrollPeriodService payrollPeriodService;
    private final EmpPayrollRunRepository empPayrollRunRepository;

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
            entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
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

        Usr createdByUser  = usrRepository.getReferenceById(requestDTO.getCreatedBy());
        Usr modifiedByUser = usrRepository.getReferenceById(requestDTO.getModifiedBy());

        for (EmployeeFixedAllowanceAssignRequestDTO.Selection selection : requestDTO.getSelections()) {
            EmployeeFixedAllowance entity = existingByFaId.get(selection.getFaId());
            if (entity != null) {
                entity.setAmount(selection.getAmount());
                entity.setModifiedBy(modifiedByUser);
                employeeFixedAllowanceRepository.save(entity);
            } else {
                employeeFixedAllowanceRepository.save(EmployeeFixedAllowance.builder()
                        .employee(employeeRepository.getReferenceById(empId))
                        .fixedAllowance(fixedAllowanceRepository.getReferenceById(selection.getFaId()))
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

    private void setRelationships(EmployeeFixedAllowance entity, EmployeeFixedAllowanceRequestDTO dto) {
        entity.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        entity.setFixedAllowance(fixedAllowanceRepository.getReferenceById(dto.getFaId()));
        entity.setCreatedBy(usrRepository.getReferenceById(dto.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }

    private void updateRelationships(EmployeeFixedAllowance entity, EmployeeFixedAllowanceRequestDTO dto) {
        if (dto.getEmpId() != null)
            entity.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        if (dto.getFaId() != null)
            entity.setFixedAllowance(fixedAllowanceRepository.getReferenceById(dto.getFaId()));
        if (dto.getModifiedBy() != null)
            entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }
}
