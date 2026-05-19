package com.payroll.service.impl;

import com.payroll.dto.request.FixedDeductionRequestDTO;
import com.payroll.dto.response.FixedDeductionResponseDTO;
import com.payroll.entity.FixedDeduction;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.FixedDeductionMapper;
import com.payroll.repository.FixedDeductionRepository;
import com.payroll.service.FixedDeductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Sort;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FixedDeductionServiceImpl implements FixedDeductionService {

    private final FixedDeductionRepository fixedDeductionRepository;
    private final FixedDeductionMapper fixedDeductionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<FixedDeductionResponseDTO> getAllFixedDeductions(boolean showDefaultRow, String isActive) {
        if (!isActive.equals("true") && !isActive.equals("false") && !isActive.equals("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<FixedDeduction> records = "all".equals(isActive)
                ? fixedDeductionRepository.findAll(sort)
                : fixedDeductionRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(fixedDeductionMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FixedDeductionResponseDTO getFixedDeductionById(Long id) {
        FixedDeduction fixedDeduction = fixedDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedDeduction", "id", id));
        return fixedDeductionMapper.toResponseDTO(fixedDeduction);
    }

    @Override
    public FixedDeductionResponseDTO createFixedDeduction(FixedDeductionRequestDTO requestDTO) {
        if (fixedDeductionRepository.existsByCodeIgnoreCase(requestDTO.getCode())) {
            throw new IllegalArgumentException(
                    "A fixed deduction with code '" + requestDTO.getCode() + "' already exists.");
        }
        FixedDeduction entity = fixedDeductionMapper.toEntity(requestDTO);
        return fixedDeductionMapper.toResponseDTO(fixedDeductionRepository.save(entity));
    }

    @Override
    public FixedDeductionResponseDTO updateFixedDeduction(Long id, FixedDeductionRequestDTO requestDTO) {
        FixedDeduction existing = fixedDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedDeduction", "id", id));
        fixedDeductionMapper.updateEntityFromDTO(requestDTO, existing);
        return fixedDeductionMapper.toResponseDTO(fixedDeductionRepository.save(existing));
    }

    @Override
    public void deleteFixedDeduction(Long id) {
        FixedDeduction fixedDeduction = fixedDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedDeduction", "id", id));
        fixedDeductionRepository.delete(fixedDeduction);
    }
}
