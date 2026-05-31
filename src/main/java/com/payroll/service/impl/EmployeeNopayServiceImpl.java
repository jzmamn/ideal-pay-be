package com.payroll.service.impl;

import com.payroll.dto.request.EmployeeNopayRequestDTO;
import com.payroll.dto.response.EmployeeNopayResponseDTO;
import com.payroll.entity.EmployeeNopay;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.EmployeeNopayMapper;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.EmployeeNopayRepository;
import com.payroll.repository.NopayDaysRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.EmployeeNopayService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeNopayServiceImpl implements EmployeeNopayService {

    private final EmployeeNopayRepository employeeNopayRepository;
    private final EmployeeNopayMapper employeeNopayMapper;
    private final EmployeeRepository employeeRepository;
    private final NopayDaysRepository nopayDaysRepository;
    private final UsrRepository usrRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeNopayResponseDTO> getAllEmployeeNopays(boolean showDefaultRow) {
        Sort sort = Sort.by("id").ascending();
        return employeeNopayRepository.findAll(sort).stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(employeeNopayMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeNopayResponseDTO getEmployeeNopayById(Long id) {
        EmployeeNopay entity = employeeNopayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeNopay", "id", id));
        return employeeNopayMapper.toResponseDTO(entity);
    }

    @Override
    public EmployeeNopayResponseDTO createEmployeeNopay(EmployeeNopayRequestDTO requestDTO) {
        EmployeeNopay entity = employeeNopayMapper.toEntity(requestDTO);
        setRelationships(entity, requestDTO);
        return employeeNopayMapper.toResponseDTO(employeeNopayRepository.save(entity));
    }

    @Override
    public EmployeeNopayResponseDTO updateEmployeeNopay(Long id, EmployeeNopayRequestDTO requestDTO) {
        EmployeeNopay existing = employeeNopayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeNopay", "id", id));
        employeeNopayMapper.updateEntityFromDTO(requestDTO, existing);
        updateRelationships(existing, requestDTO);
        return employeeNopayMapper.toResponseDTO(employeeNopayRepository.save(existing));
    }

    @Override
    public void deleteEmployeeNopay(Long id) {
        EmployeeNopay entity = employeeNopayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeNopay", "id", id));
        employeeNopayRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeNopayResponseDTO> getByEmployeeId(Long empId) {
        Sort sort = Sort.by("id").ascending();
        return employeeNopayRepository.findAllByEmployeeId(empId, sort).stream()
                .map(employeeNopayMapper::toResponseDTO)
                .toList();
    }

    private void setRelationships(EmployeeNopay entity, EmployeeNopayRequestDTO dto) {
        entity.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        entity.setNopayDays(nopayDaysRepository.getReferenceById(dto.getNopayId()));
        entity.setCreatedBy(usrRepository.getReferenceById(dto.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }

    private void updateRelationships(EmployeeNopay entity, EmployeeNopayRequestDTO dto) {
        if (dto.getEmpId() != null)
            entity.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        if (dto.getNopayId() != null)
            entity.setNopayDays(nopayDaysRepository.getReferenceById(dto.getNopayId()));
        if (dto.getModifiedBy() != null)
            entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }
}
