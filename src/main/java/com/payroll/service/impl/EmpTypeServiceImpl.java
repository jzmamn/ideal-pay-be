package com.payroll.service.impl;

import com.payroll.dto.request.EmpTypeRequestDTO;
import com.payroll.dto.response.EmpTypeResponseDTO;
import com.payroll.entity.EmpType;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.EmpTypeMapper;
import com.payroll.repository.EmpTypeRepository;
import com.payroll.service.EmpTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmpTypeServiceImpl implements EmpTypeService {

    private final EmpTypeRepository empTypeRepository;
    private final EmpTypeMapper empTypeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<EmpTypeResponseDTO> getAllEmpTypes(boolean showDefaultRow, String isActive) {
        if (!isActive.equals("true") && !isActive.equals("false") && !isActive.equals("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<EmpType> records = "all".equals(isActive)
                ? empTypeRepository.findAll(sort)
                : empTypeRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(empTypeMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmpTypeResponseDTO getEmpTypeById(Long id) {
        EmpType empType = empTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmpType", "id", id));
        return empTypeMapper.toResponseDTO(empType);
    }

    @Override
    public EmpTypeResponseDTO createEmpType(EmpTypeRequestDTO requestDTO) {
        if (empTypeRepository.existsByCodeIgnoreCase(requestDTO.getCode())) {
            throw new IllegalArgumentException(
                    "An employee type with code '" + requestDTO.getCode() + "' already exists.");
        }
        EmpType entity = empTypeMapper.toEntity(requestDTO);
        return empTypeMapper.toResponseDTO(empTypeRepository.save(entity));
    }

    @Override
    public EmpTypeResponseDTO updateEmpType(Long id, EmpTypeRequestDTO requestDTO) {
        EmpType existing = empTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmpType", "id", id));
        empTypeMapper.updateEntityFromDTO(requestDTO, existing);
        return empTypeMapper.toResponseDTO(empTypeRepository.save(existing));
    }

    @Override
    public void deleteEmpType(Long id) {
        EmpType empType = empTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmpType", "id", id));
        empTypeRepository.delete(empType);
    }
}
