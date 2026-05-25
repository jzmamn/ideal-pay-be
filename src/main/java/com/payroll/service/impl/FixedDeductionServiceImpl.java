package com.payroll.service.impl;

import com.payroll.dto.request.FixedDeductionRequestDTO;
import com.payroll.dto.response.FixedDeductionResponseDTO;
import com.payroll.entity.FixedDeduction;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.FixedDeductionMapper;
import com.payroll.repository.FixedDeductionRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.FixedDeductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FixedDeductionServiceImpl implements FixedDeductionService {

    private final FixedDeductionRepository fixedDeductionRepository;
    private final UsrRepository usrRepository;
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
        FixedDeduction entity = fixedDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedDeduction", "id", id));
        return fixedDeductionMapper.toResponseDTO(entity);
    }

    @Override
    public FixedDeductionResponseDTO createFixedDeduction(FixedDeductionRequestDTO requestDTO) {
        FixedDeduction entity = fixedDeductionMapper.toEntity(requestDTO);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        FixedDeduction saved = fixedDeductionRepository.save(entity);
        saved.setCode("FD_" + saved.getId());
        return fixedDeductionMapper.toResponseDTO(fixedDeductionRepository.save(saved));
    }

    @Override
    public FixedDeductionResponseDTO updateFixedDeduction(Long id, FixedDeductionRequestDTO requestDTO) {
        FixedDeduction existing = fixedDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedDeduction", "id", id));
        fixedDeductionMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return fixedDeductionMapper.toResponseDTO(fixedDeductionRepository.save(existing));
    }

    @Override
    public void deleteFixedDeduction(Long id) {
        FixedDeduction entity = fixedDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedDeduction", "id", id));
        fixedDeductionRepository.delete(entity);
    }
}
