package com.payroll.service.impl;

import com.payroll.dto.request.EmployeeSalaryAdvanceRequestDTO;
import com.payroll.dto.response.EmployeeSalaryAdvanceResponseDTO;
import com.payroll.entity.EmployeeSalaryAdvance;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.EmployeeSalaryAdvanceMapper;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.EmployeeSalaryAdvanceRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.EmployeeSalaryAdvanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeSalaryAdvanceServiceImpl implements EmployeeSalaryAdvanceService {

    private final EmployeeSalaryAdvanceRepository salaryAdvanceRepository;
    private final EmployeeSalaryAdvanceMapper salaryAdvanceMapper;
    private final EmployeeRepository employeeRepository;
    private final UsrRepository usrRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSalaryAdvanceResponseDTO> getAllSalaryAdvances(boolean showDefaultRow) {
        Sort sort = Sort.by("id").ascending();
        return salaryAdvanceRepository.findAll(sort).stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(salaryAdvanceMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeSalaryAdvanceResponseDTO getSalaryAdvanceById(Long id) {
        EmployeeSalaryAdvance entity = salaryAdvanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeSalaryAdvance", "id", id));
        return salaryAdvanceMapper.toResponseDTO(entity);
    }

    @Override
    public EmployeeSalaryAdvanceResponseDTO createSalaryAdvance(EmployeeSalaryAdvanceRequestDTO requestDTO) {
        EmployeeSalaryAdvance entity = salaryAdvanceMapper.toEntity(requestDTO);
        setRelationships(entity, requestDTO);
        return salaryAdvanceMapper.toResponseDTO(salaryAdvanceRepository.save(entity));
    }

    @Override
    public EmployeeSalaryAdvanceResponseDTO updateSalaryAdvance(Long id, EmployeeSalaryAdvanceRequestDTO requestDTO) {
        EmployeeSalaryAdvance existing = salaryAdvanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeSalaryAdvance", "id", id));
        salaryAdvanceMapper.updateEntityFromDTO(requestDTO, existing);
        updateRelationships(existing, requestDTO);
        return salaryAdvanceMapper.toResponseDTO(salaryAdvanceRepository.save(existing));
    }

    @Override
    public void deleteSalaryAdvance(Long id) {
        EmployeeSalaryAdvance entity = salaryAdvanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeSalaryAdvance", "id", id));
        salaryAdvanceRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSalaryAdvanceResponseDTO> getByEmployeeId(Long empId) {
        Sort sort = Sort.by("id").ascending();
        return salaryAdvanceRepository.findAllByEmployeeId(empId, sort).stream()
                .map(salaryAdvanceMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSalaryAdvanceResponseDTO> getByPayrollMonth(String payrollMonth) {
        Sort sort = Sort.by("id").ascending();
        return salaryAdvanceRepository.findAllByPayrollMonth(payrollMonth, sort).stream()
                .map(salaryAdvanceMapper::toResponseDTO)
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setRelationships(EmployeeSalaryAdvance entity, EmployeeSalaryAdvanceRequestDTO dto) {
        entity.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        entity.setCreatedBy(usrRepository.getReferenceById(dto.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }

    private void updateRelationships(EmployeeSalaryAdvance entity, EmployeeSalaryAdvanceRequestDTO dto) {
        if (dto.getEmpId() != null)
            entity.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        if (dto.getModifiedBy() != null)
            entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }
}
