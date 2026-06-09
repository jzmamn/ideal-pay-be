package com.payroll.service.impl;

import com.payroll.dto.request.EmployeeLateRequestDTO;
import com.payroll.dto.response.EmployeeLateResponseDTO;
import com.payroll.entity.EmployeeLate;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.EmployeeLateMapper;
import com.payroll.repository.EmployeeLateRepository;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.EmployeeLateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeLateServiceImpl implements EmployeeLateService {

    private final EmployeeLateRepository employeeLateRepository;
    private final EmployeeLateMapper     employeeLateMapper;
    private final EmployeeRepository     employeeRepository;
    private final UsrRepository          usrRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeLateResponseDTO> getAllEmployeeLates(boolean showDefaultRow) {
        Sort sort = Sort.by("id").ascending();
        return employeeLateRepository.findAll(sort).stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(employeeLateMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeLateResponseDTO getEmployeeLateById(Long id) {
        EmployeeLate entity = employeeLateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeLate", "id", id));
        return employeeLateMapper.toResponseDTO(entity);
    }

    @Override
    public EmployeeLateResponseDTO createEmployeeLate(EmployeeLateRequestDTO requestDTO) {
        // Upsert: one record per employee per payroll month
        EmployeeLate entity = employeeLateRepository
                .findByEmployee_IdAndPayrollMonth(requestDTO.getEmpId(), requestDTO.getPayrollMonth())
                .orElse(null);

        if (entity != null) {
            entity.setHours(requestDTO.getHours());
            entity.setAmount(requestDTO.getAmount());
            entity.setIsProcessed(requestDTO.getIsProcessed() != null ? requestDTO.getIsProcessed() : Boolean.FALSE);
            entity.setProcessedDate(requestDTO.getProcessedDate());
            entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        } else {
            entity = employeeLateMapper.toEntity(requestDTO);
            setRelationships(entity, requestDTO);
        }

        return employeeLateMapper.toResponseDTO(employeeLateRepository.save(entity));
    }

    @Override
    public EmployeeLateResponseDTO updateEmployeeLate(Long id, EmployeeLateRequestDTO requestDTO) {
        EmployeeLate existing = employeeLateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeLate", "id", id));
        employeeLateMapper.updateEntityFromDTO(requestDTO, existing);
        updateRelationships(existing, requestDTO);
        return employeeLateMapper.toResponseDTO(employeeLateRepository.save(existing));
    }

    @Override
    public void deleteEmployeeLate(Long id) {
        EmployeeLate entity = employeeLateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeLate", "id", id));
        employeeLateRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeLateResponseDTO> getByEmployeeId(Long empId) {
        Sort sort = Sort.by("id").ascending();
        return employeeLateRepository.findAllByEmployeeId(empId, sort).stream()
                .map(employeeLateMapper::toResponseDTO)
                .toList();
    }

    private void setRelationships(EmployeeLate entity, EmployeeLateRequestDTO dto) {
        entity.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        entity.setCreatedBy(usrRepository.getReferenceById(dto.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }

    private void updateRelationships(EmployeeLate entity, EmployeeLateRequestDTO dto) {
        if (dto.getEmpId() != null)
            entity.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        if (dto.getModifiedBy() != null)
            entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }
}
