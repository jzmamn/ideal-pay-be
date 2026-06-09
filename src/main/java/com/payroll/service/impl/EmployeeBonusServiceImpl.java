package com.payroll.service.impl;

import com.payroll.dto.request.EmployeeBonusRequestDTO;
import com.payroll.dto.response.EmployeeBonusResponseDTO;
import com.payroll.entity.EmployeeBonus;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.EmployeeBonusMapper;
import com.payroll.repository.BonusRepository;
import com.payroll.repository.EmployeeBonusRepository;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.EmployeeBonusService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeBonusServiceImpl implements EmployeeBonusService {

    private final EmployeeBonusRepository repository;
    private final EmployeeBonusMapper mapper;
    private final EmployeeRepository employeeRepository;
    private final UsrRepository usrRepository;
    private final BonusRepository bonusRepository;

    @Override @Transactional(readOnly = true)
    public List<EmployeeBonusResponseDTO> getAll(boolean showDefaultRow) {
        return repository.findAll(Sort.by("id").ascending()).stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(mapper::toResponseDTO).toList();
    }

    @Override @Transactional(readOnly = true)
    public EmployeeBonusResponseDTO getById(Long id) {
        return mapper.toResponseDTO(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeBonus", "id", id)));
    }

    @Override
    public EmployeeBonusResponseDTO create(EmployeeBonusRequestDTO dto) {
        EmployeeBonus entity = mapper.toEntity(dto);
        setRelationships(entity, dto);
        return mapper.toResponseDTO(repository.save(entity));
    }

    @Override
    public EmployeeBonusResponseDTO update(Long id, EmployeeBonusRequestDTO dto) {
        EmployeeBonus existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeBonus", "id", id));
        mapper.updateEntityFromDTO(dto, existing);
        updateRelationships(existing, dto);
        return mapper.toResponseDTO(repository.save(existing));
    }

    @Override
    public void delete(Long id) {
        repository.delete(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeBonus", "id", id)));
    }

    @Override @Transactional(readOnly = true)
    public List<EmployeeBonusResponseDTO> getByEmployeeId(Long empId) {
        return repository.findAllByEmployeeId(empId, Sort.by("id").ascending())
                .stream().map(mapper::toResponseDTO).toList();
    }

    @Override @Transactional(readOnly = true)
    public List<EmployeeBonusResponseDTO> getByPayrollMonth(String payrollMonth) {
        return repository.findAllByPayrollMonth(payrollMonth, Sort.by("id").ascending())
                .stream().map(mapper::toResponseDTO).toList();
    }

    private void setRelationships(EmployeeBonus e, EmployeeBonusRequestDTO dto) {
        e.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        e.setCreatedBy(usrRepository.getReferenceById(dto.getCreatedBy()));
        e.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
        if (dto.getBonusId() != null) {
            e.setBonus(bonusRepository.getReferenceById(dto.getBonusId()));
        }
    }

    private void updateRelationships(EmployeeBonus e, EmployeeBonusRequestDTO dto) {
        if (dto.getEmpId()      != null) e.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        if (dto.getModifiedBy() != null) e.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
        if (dto.getBonusId()    != null) {
            e.setBonus(bonusRepository.getReferenceById(dto.getBonusId()));
        }
    }
}
