package com.payroll.service.impl;

import com.payroll.dto.request.EmployeeFixedAllowanceRequestDTO;
import com.payroll.dto.response.EmployeeFixedAllowanceResponseDTO;
import com.payroll.entity.EmployeeFixedAllowance;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.EmployeeFixedAllowanceMapper;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.EmployeeFixedAllowanceRepository;
import com.payroll.repository.FixedAllowanceRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.EmployeeFixedAllowanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeFixedAllowanceServiceImpl implements EmployeeFixedAllowanceService {

    private final EmployeeFixedAllowanceRepository employeeFixedAllowanceRepository;
    private final EmployeeFixedAllowanceMapper employeeFixedAllowanceMapper;
    private final EmployeeRepository employeeRepository;
    private final FixedAllowanceRepository fixedAllowanceRepository;
    private final UsrRepository usrRepository;

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
        EmployeeFixedAllowance entity = employeeFixedAllowanceMapper.toEntity(requestDTO);
        setRelationships(entity, requestDTO);
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
