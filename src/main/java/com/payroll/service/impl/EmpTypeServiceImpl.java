package com.payroll.service.impl;

import com.payroll.dto.request.EmpTypeRequestDTO;
import com.payroll.dto.response.EmpTypeResponseDTO;
import com.payroll.entity.EmpType;
import com.payroll.entity.Usr;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.EmpTypeMapper;
import com.payroll.repository.EmpTypeRepository;
import com.payroll.repository.UsrRepository;
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
    private final UsrRepository usrRepository;
    private final EmpTypeMapper empTypeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<EmpTypeResponseDTO> getAllEmpTypes(boolean showDefaultRow, String isActive) {
        if (!isActive.equalsIgnoreCase("true") && !isActive.equalsIgnoreCase("false") && !isActive.equalsIgnoreCase("all")) {
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
        EmpType entity = empTypeMapper.toEntity(requestDTO);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        // Auto-generate code as EMT_<id>
        EmpType saved = empTypeRepository.save(entity);
        saved.setCode("EMT_" + saved.getId());
        return empTypeMapper.toResponseDTO(empTypeRepository.save(saved));
    }

    @Override
    public EmpTypeResponseDTO updateEmpType(Long id, EmpTypeRequestDTO requestDTO) {
        EmpType existing = empTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmpType", "id", id));
        empTypeMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return empTypeMapper.toResponseDTO(empTypeRepository.save(existing));
    }

    @Override
    public void deleteEmpType(Long id) {
        EmpType empType = empTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmpType", "id", id));
        empTypeRepository.delete(empType);
    }
}
