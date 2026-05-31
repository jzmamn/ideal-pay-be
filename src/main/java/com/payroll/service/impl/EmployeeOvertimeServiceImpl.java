package com.payroll.service.impl;

import com.payroll.dto.request.EmployeeOvertimeRequestDTO;
import com.payroll.dto.response.EmployeeOvertimeResponseDTO;
import com.payroll.entity.EmployeeOvertime;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.EmployeeOvertimeMapper;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.EmployeeOvertimeRepository;
import com.payroll.repository.OvertimeRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.EmployeeOvertimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeOvertimeServiceImpl implements EmployeeOvertimeService {

    private final EmployeeOvertimeRepository employeeOvertimeRepository;
    private final EmployeeOvertimeMapper employeeOvertimeMapper;
    private final EmployeeRepository employeeRepository;
    private final OvertimeRepository overtimeRepository;
    private final UsrRepository usrRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeOvertimeResponseDTO> getAllEmployeeOvertimes(boolean showDefaultRow) {
        Sort sort = Sort.by("id").ascending();
        return employeeOvertimeRepository.findAll(sort).stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(employeeOvertimeMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeOvertimeResponseDTO getEmployeeOvertimeById(Long id) {
        EmployeeOvertime entity = employeeOvertimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeOvertime", "id", id));
        return employeeOvertimeMapper.toResponseDTO(entity);
    }

    @Override
    public EmployeeOvertimeResponseDTO createEmployeeOvertime(EmployeeOvertimeRequestDTO requestDTO) {
        EmployeeOvertime entity = employeeOvertimeMapper.toEntity(requestDTO);
        setRelationships(entity, requestDTO);
        return employeeOvertimeMapper.toResponseDTO(employeeOvertimeRepository.save(entity));
    }

    @Override
    public EmployeeOvertimeResponseDTO updateEmployeeOvertime(Long id, EmployeeOvertimeRequestDTO requestDTO) {
        EmployeeOvertime existing = employeeOvertimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeOvertime", "id", id));
        employeeOvertimeMapper.updateEntityFromDTO(requestDTO, existing);
        updateRelationships(existing, requestDTO);
        return employeeOvertimeMapper.toResponseDTO(employeeOvertimeRepository.save(existing));
    }

    @Override
    public void deleteEmployeeOvertime(Long id) {
        EmployeeOvertime entity = employeeOvertimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeOvertime", "id", id));
        employeeOvertimeRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeOvertimeResponseDTO> getByEmployeeId(Long empId) {
        Sort sort = Sort.by("id").ascending();
        return employeeOvertimeRepository.findAllByEmployeeId(empId, sort).stream()
                .map(employeeOvertimeMapper::toResponseDTO)
                .toList();
    }

    private void setRelationships(EmployeeOvertime entity, EmployeeOvertimeRequestDTO dto) {
        entity.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        entity.setOvertime(overtimeRepository.getReferenceById(dto.getOvertimeId()));
        entity.setCreatedBy(usrRepository.getReferenceById(dto.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }

    private void updateRelationships(EmployeeOvertime entity, EmployeeOvertimeRequestDTO dto) {
        if (dto.getEmpId() != null)
            entity.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        if (dto.getOvertimeId() != null)
            entity.setOvertime(overtimeRepository.getReferenceById(dto.getOvertimeId()));
        if (dto.getModifiedBy() != null)
            entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }
}
