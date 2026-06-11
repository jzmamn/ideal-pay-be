package com.payroll.service.impl;

import com.payroll.dto.request.EmployeeVariableAllowanceRequestDTO;
import com.payroll.dto.response.EmployeeVariableAllowanceResponseDTO;
import com.payroll.entity.EmployeeVariableAllowance;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.EmployeeVariableAllowanceMapper;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.EmployeeVariableAllowanceRepository;
import com.payroll.repository.VariableAllowanceRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.EmployeeVariableAllowanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeVariableAllowanceServiceImpl implements EmployeeVariableAllowanceService {

    private final EmployeeVariableAllowanceRepository employeeVariableAllowanceRepository;
    private final EmployeeVariableAllowanceMapper employeeVariableAllowanceMapper;
    private final EmployeeRepository employeeRepository;
    private final VariableAllowanceRepository variableAllowanceRepository;
    private final UsrRepository usrRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeVariableAllowanceResponseDTO> getAllEmployeeVariableAllowances(boolean showDefaultRow) {
        Sort sort = Sort.by("id").ascending();
        return employeeVariableAllowanceRepository.findAll(sort).stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(employeeVariableAllowanceMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeVariableAllowanceResponseDTO getEmployeeVariableAllowanceById(Long id) {
        EmployeeVariableAllowance entity = employeeVariableAllowanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeVariableAllowance", "id", id));
        return employeeVariableAllowanceMapper.toResponseDTO(entity);
    }

    @Override
    public EmployeeVariableAllowanceResponseDTO createEmployeeVariableAllowance(EmployeeVariableAllowanceRequestDTO requestDTO) {
        EmployeeVariableAllowance entity = employeeVariableAllowanceRepository
                .findByEmployee_IdAndVariableAllowance_IdAndPayrollMonth(
                        requestDTO.getEmpId(), requestDTO.getVaId(), requestDTO.getPayrollMonth())
                .orElse(null);

        if (entity != null) {
            entity.setAmount(requestDTO.getAmount());
            entity.setIsProcessed(requestDTO.getIsProcessed() != null ? requestDTO.getIsProcessed() : Boolean.FALSE);
            entity.setProcessedDate(requestDTO.getProcessedDate());
            entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        } else {
            entity = employeeVariableAllowanceMapper.toEntity(requestDTO);
            setRelationships(entity, requestDTO);
        }

        return employeeVariableAllowanceMapper.toResponseDTO(employeeVariableAllowanceRepository.save(entity));
    }

    @Override
    public EmployeeVariableAllowanceResponseDTO updateEmployeeVariableAllowance(Long id, EmployeeVariableAllowanceRequestDTO requestDTO) {
        EmployeeVariableAllowance existing = employeeVariableAllowanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeVariableAllowance", "id", id));
        employeeVariableAllowanceMapper.updateEntityFromDTO(requestDTO, existing);
        updateRelationships(existing, requestDTO);
        return employeeVariableAllowanceMapper.toResponseDTO(employeeVariableAllowanceRepository.save(existing));
    }

    @Override
    public void deleteEmployeeVariableAllowance(Long id) {
        EmployeeVariableAllowance entity = employeeVariableAllowanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeVariableAllowance", "id", id));
        employeeVariableAllowanceRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeVariableAllowanceResponseDTO> getByEmployeeId(Long empId) {
        Sort sort = Sort.by("id").ascending();
        return employeeVariableAllowanceRepository.findAllByEmployeeId(empId, sort).stream()
                .map(employeeVariableAllowanceMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeVariableAllowanceResponseDTO> getByEmployeeId(Long empId, String payrollMonth) {
        return employeeVariableAllowanceRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth).stream()
                .map(employeeVariableAllowanceMapper::toResponseDTO)
                .toList();
    }

    private void setRelationships(EmployeeVariableAllowance entity, EmployeeVariableAllowanceRequestDTO dto) {
        entity.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        entity.setVariableAllowance(variableAllowanceRepository.getReferenceById(dto.getVaId()));
        entity.setCreatedBy(usrRepository.getReferenceById(dto.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }

    private void updateRelationships(EmployeeVariableAllowance entity, EmployeeVariableAllowanceRequestDTO dto) {
        if (dto.getEmpId() != null)
            entity.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        if (dto.getVaId() != null)
            entity.setVariableAllowance(variableAllowanceRepository.getReferenceById(dto.getVaId()));
        if (dto.getModifiedBy() != null)
            entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }
}
