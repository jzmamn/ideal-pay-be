package com.payroll.service.impl;

import com.payroll.dto.request.VariableAllowanceRequestDTO;
import com.payroll.dto.response.VariableAllowanceResponseDTO;
import com.payroll.entity.VariableAllowance;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.VariableAllowanceMapper;
import com.payroll.repository.VariableAllowanceRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.VariableAllowanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VariableAllowanceServiceImpl implements VariableAllowanceService {

    private final VariableAllowanceRepository variableAllowanceRepository;
    private final UsrRepository usrRepository;
    private final VariableAllowanceMapper variableAllowanceMapper;

    @Override
    @Transactional(readOnly = true)
    public List<VariableAllowanceResponseDTO> getAllVariableAllowances(boolean showDefaultRow, String isActive) {
        if (!isActive.equalsIgnoreCase("true") && !isActive.equalsIgnoreCase("false") && !isActive.equalsIgnoreCase("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<VariableAllowance> records = "all".equalsIgnoreCase(isActive)
                ? variableAllowanceRepository.findAll(sort)
                : variableAllowanceRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(variableAllowanceMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VariableAllowanceResponseDTO getVariableAllowanceById(Long id) {
        VariableAllowance entity = variableAllowanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VariableAllowance", "id", id));
        return variableAllowanceMapper.toResponseDTO(entity);
    }

    @Override
    public VariableAllowanceResponseDTO createVariableAllowance(VariableAllowanceRequestDTO requestDTO) {
        VariableAllowance entity = variableAllowanceMapper.toEntity(requestDTO);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        VariableAllowance saved = variableAllowanceRepository.save(entity);
        saved.setCode("VA_" + saved.getId());
        return variableAllowanceMapper.toResponseDTO(variableAllowanceRepository.save(saved));
    }

    @Override
    public VariableAllowanceResponseDTO updateVariableAllowance(Long id, VariableAllowanceRequestDTO requestDTO) {
        VariableAllowance existing = variableAllowanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VariableAllowance", "id", id));
        variableAllowanceMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return variableAllowanceMapper.toResponseDTO(variableAllowanceRepository.save(existing));
    }

    @Override
    public void deleteVariableAllowance(Long id) {
        VariableAllowance entity = variableAllowanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VariableAllowance", "id", id));
        variableAllowanceRepository.delete(entity);
    }
}
