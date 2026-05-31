package com.payroll.service.impl;

import com.payroll.dto.request.EmployeeVariableDeductionRequestDTO;
import com.payroll.dto.response.EmployeeVariableDeductionResponseDTO;
import com.payroll.entity.EmployeeVariableDeduction;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.EmployeeVariableDeductionMapper;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.EmployeeVariableDeductionRepository;
import com.payroll.repository.VariableDeductionRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.EmployeeVariableDeductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeVariableDeductionServiceImpl implements EmployeeVariableDeductionService {

    private final EmployeeVariableDeductionRepository employeeVariableDeductionRepository;
    private final EmployeeVariableDeductionMapper employeeVariableDeductionMapper;
    private final EmployeeRepository employeeRepository;
    private final VariableDeductionRepository variableDeductionRepository;
    private final UsrRepository usrRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeVariableDeductionResponseDTO> getAllEmployeeVariableDeductions(boolean showDefaultRow) {
        Sort sort = Sort.by("id").ascending();
        return employeeVariableDeductionRepository.findAll(sort).stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(employeeVariableDeductionMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeVariableDeductionResponseDTO getEmployeeVariableDeductionById(Long id) {
        EmployeeVariableDeduction entity = employeeVariableDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeVariableDeduction", "id", id));
        return employeeVariableDeductionMapper.toResponseDTO(entity);
    }

    @Override
    public EmployeeVariableDeductionResponseDTO createEmployeeVariableDeduction(EmployeeVariableDeductionRequestDTO requestDTO) {
        EmployeeVariableDeduction entity = employeeVariableDeductionMapper.toEntity(requestDTO);
        setRelationships(entity, requestDTO);
        return employeeVariableDeductionMapper.toResponseDTO(employeeVariableDeductionRepository.save(entity));
    }

    @Override
    public EmployeeVariableDeductionResponseDTO updateEmployeeVariableDeduction(Long id, EmployeeVariableDeductionRequestDTO requestDTO) {
        EmployeeVariableDeduction existing = employeeVariableDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeVariableDeduction", "id", id));
        employeeVariableDeductionMapper.updateEntityFromDTO(requestDTO, existing);
        updateRelationships(existing, requestDTO);
        return employeeVariableDeductionMapper.toResponseDTO(employeeVariableDeductionRepository.save(existing));
    }

    @Override
    public void deleteEmployeeVariableDeduction(Long id) {
        EmployeeVariableDeduction entity = employeeVariableDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeVariableDeduction", "id", id));
        employeeVariableDeductionRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeVariableDeductionResponseDTO> getByEmployeeId(Long empId) {
        Sort sort = Sort.by("id").ascending();
        return employeeVariableDeductionRepository.findAllByEmployeeId(empId, sort).stream()
                .map(employeeVariableDeductionMapper::toResponseDTO)
                .toList();
    }

    private void setRelationships(EmployeeVariableDeduction entity, EmployeeVariableDeductionRequestDTO dto) {
        entity.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        entity.setVariableDeduction(variableDeductionRepository.getReferenceById(dto.getVdId()));
        entity.setCreatedBy(usrRepository.getReferenceById(dto.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }

    private void updateRelationships(EmployeeVariableDeduction entity, EmployeeVariableDeductionRequestDTO dto) {
        if (dto.getEmpId() != null)
            entity.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        if (dto.getVdId() != null)
            entity.setVariableDeduction(variableDeductionRepository.getReferenceById(dto.getVdId()));
        if (dto.getModifiedBy() != null)
            entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }
}
