package com.payroll.service.impl;

import com.payroll.dto.request.EmployeeSalaryIncrementRequestDTO;
import com.payroll.dto.response.EmployeeSalaryIncrementResponseDTO;
import com.payroll.entity.EmployeeSalaryIncrement;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.EmployeeSalaryIncrementMapper;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.EmployeeSalaryIncrementRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.EmployeeSalaryIncrementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeSalaryIncrementServiceImpl implements EmployeeSalaryIncrementService {

    private final EmployeeSalaryIncrementRepository repository;
    private final EmployeeSalaryIncrementMapper mapper;
    private final EmployeeRepository employeeRepository;
    private final UsrRepository usrRepository;

    @Override @Transactional(readOnly = true)
    public List<EmployeeSalaryIncrementResponseDTO> getAll(boolean showDefaultRow) {
        return repository.findAll(Sort.by("id").ascending()).stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(mapper::toResponseDTO).toList();
    }

    @Override @Transactional(readOnly = true)
    public EmployeeSalaryIncrementResponseDTO getById(Long id) {
        return mapper.toResponseDTO(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeSalaryIncrement", "id", id)));
    }

    @Override
    public EmployeeSalaryIncrementResponseDTO create(EmployeeSalaryIncrementRequestDTO dto) {
        EmployeeSalaryIncrement entity = mapper.toEntity(dto);
        setRelationships(entity, dto);
        return mapper.toResponseDTO(repository.save(entity));
    }

    @Override
    public EmployeeSalaryIncrementResponseDTO update(Long id, EmployeeSalaryIncrementRequestDTO dto) {
        EmployeeSalaryIncrement existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeSalaryIncrement", "id", id));
        mapper.updateEntityFromDTO(dto, existing);
        updateRelationships(existing, dto);
        return mapper.toResponseDTO(repository.save(existing));
    }

    @Override
    public void delete(Long id) {
        repository.delete(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeSalaryIncrement", "id", id)));
    }

    @Override @Transactional(readOnly = true)
    public List<EmployeeSalaryIncrementResponseDTO> getByEmployeeId(Long empId) {
        return repository.findAllByEmployeeId(empId, Sort.by("id").ascending())
                .stream().map(mapper::toResponseDTO).toList();
    }

    @Override @Transactional(readOnly = true)
    public List<EmployeeSalaryIncrementResponseDTO> getByPayrollMonth(String payrollMonth) {
        return repository.findAllByPayrollMonth(payrollMonth, Sort.by("id").ascending())
                .stream().map(mapper::toResponseDTO).toList();
    }

    private void setRelationships(EmployeeSalaryIncrement e, EmployeeSalaryIncrementRequestDTO dto) {
        e.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        e.setCreatedBy(usrRepository.getReferenceById(dto.getCreatedBy()));
        e.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }

    private void updateRelationships(EmployeeSalaryIncrement e, EmployeeSalaryIncrementRequestDTO dto) {
        if (dto.getEmpId()      != null) e.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        if (dto.getModifiedBy() != null) e.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }
}
