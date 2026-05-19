package com.payroll.service.impl;

import com.payroll.dto.request.VariableDeductionRequestDTO;
import com.payroll.dto.response.VariableDeductionResponseDTO;
import com.payroll.entity.VariableDeduction;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.VariableDeductionMapper;
import com.payroll.repository.VariableDeductionRepository;
import com.payroll.service.VariableDeductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Sort;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VariableDeductionServiceImpl implements VariableDeductionService {

    private final VariableDeductionRepository variableDeductionRepository;
    private final VariableDeductionMapper variableDeductionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<VariableDeductionResponseDTO> getAllVariableDeductions(boolean showDefaultRow, String isActive) {
        if (!isActive.equals("true") && !isActive.equals("false") && !isActive.equals("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<VariableDeduction> records = "all".equals(isActive)
                ? variableDeductionRepository.findAll(sort)
                : variableDeductionRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(variableDeductionMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VariableDeductionResponseDTO getVariableDeductionById(Long id) {
        VariableDeduction entity = variableDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VariableDeduction", "id", id));
        return variableDeductionMapper.toResponseDTO(entity);
    }

    @Override
    public VariableDeductionResponseDTO createVariableDeduction(VariableDeductionRequestDTO requestDTO) {
        if (variableDeductionRepository.existsByCodeIgnoreCase(requestDTO.getCode())) {
            throw new IllegalArgumentException(
                    "A variable deduction with code '" + requestDTO.getCode() + "' already exists.");
        }
        VariableDeduction entity = variableDeductionMapper.toEntity(requestDTO);
        return variableDeductionMapper.toResponseDTO(variableDeductionRepository.save(entity));
    }

    @Override
    public VariableDeductionResponseDTO updateVariableDeduction(Long id, VariableDeductionRequestDTO requestDTO) {
        VariableDeduction existing = variableDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VariableDeduction", "id", id));
        variableDeductionMapper.updateEntityFromDTO(requestDTO, existing);
        return variableDeductionMapper.toResponseDTO(variableDeductionRepository.save(existing));
    }

    @Override
    public void deleteVariableDeduction(Long id) {
        VariableDeduction entity = variableDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VariableDeduction", "id", id));
        variableDeductionRepository.delete(entity);
    }
}
