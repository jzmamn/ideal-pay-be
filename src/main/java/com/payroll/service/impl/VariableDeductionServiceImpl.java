package com.payroll.service.impl;

import com.payroll.dto.request.VariableDeductionRequestDTO;
import com.payroll.dto.response.VariableDeductionResponseDTO;
import com.payroll.entity.VariableDeduction;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.VariableDeductionMapper;
import com.payroll.repository.VariableDeductionRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.VariableDeductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VariableDeductionServiceImpl implements VariableDeductionService {

    private final VariableDeductionRepository variableDeductionRepository;
    private final UsrRepository usrRepository;
    private final VariableDeductionMapper variableDeductionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<VariableDeductionResponseDTO> getAllVariableDeductions(boolean showDefaultRow, String isActive) {
        if (!isActive.equalsIgnoreCase("true") && !isActive.equalsIgnoreCase("false") && !isActive.equalsIgnoreCase("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<VariableDeduction> records = "all".equalsIgnoreCase(isActive)
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
        VariableDeduction entity = variableDeductionMapper.toEntity(requestDTO);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        VariableDeduction saved = variableDeductionRepository.save(entity);
        saved.setCode("VD_" + saved.getId());
        return variableDeductionMapper.toResponseDTO(variableDeductionRepository.save(saved));
    }

    @Override
    public VariableDeductionResponseDTO updateVariableDeduction(Long id, VariableDeductionRequestDTO requestDTO) {
        VariableDeduction existing = variableDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VariableDeduction", "id", id));
        variableDeductionMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return variableDeductionMapper.toResponseDTO(variableDeductionRepository.save(existing));
    }

    @Override
    public void deleteVariableDeduction(Long id) {
        VariableDeduction entity = variableDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VariableDeduction", "id", id));
        variableDeductionRepository.delete(entity);
    }
}
