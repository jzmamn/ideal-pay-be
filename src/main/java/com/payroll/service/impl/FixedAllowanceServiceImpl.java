package com.payroll.service.impl;

import com.payroll.dto.request.FixedAllowanceRequestDTO;
import com.payroll.dto.response.FixedAllowanceResponseDTO;
import com.payroll.entity.FixedAllowance;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.FixedAllowanceMapper;
import com.payroll.repository.FixedAllowanceRepository;
import com.payroll.service.FixedAllowanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Sort;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FixedAllowanceServiceImpl implements FixedAllowanceService {

    private final FixedAllowanceRepository fixedAllowanceRepository;
    private final FixedAllowanceMapper fixedAllowanceMapper;

    @Override
    @Transactional(readOnly = true)
    public List<FixedAllowanceResponseDTO> getAllFixedAllowances(boolean showDefaultRow, String isActive) {
        if (!isActive.equals("true") && !isActive.equals("false") && !isActive.equals("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<FixedAllowance> records = "all".equals(isActive)
                ? fixedAllowanceRepository.findAll(sort)
                : fixedAllowanceRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(fixedAllowanceMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FixedAllowanceResponseDTO getFixedAllowanceById(Long id) {
        FixedAllowance fixedAllowance = fixedAllowanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedAllowance", "id", id));
        return fixedAllowanceMapper.toResponseDTO(fixedAllowance);
    }

    @Override
    public FixedAllowanceResponseDTO createFixedAllowance(FixedAllowanceRequestDTO requestDTO) {
        if (fixedAllowanceRepository.existsByCodeIgnoreCase(requestDTO.getCode())) {
            throw new IllegalArgumentException(
                    "A fixed allowance with code '" + requestDTO.getCode() + "' already exists.");
        }
        FixedAllowance entity = fixedAllowanceMapper.toEntity(requestDTO);
        return fixedAllowanceMapper.toResponseDTO(fixedAllowanceRepository.save(entity));
    }

    @Override
    public FixedAllowanceResponseDTO updateFixedAllowance(Long id, FixedAllowanceRequestDTO requestDTO) {
        FixedAllowance existing = fixedAllowanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedAllowance", "id", id));
        fixedAllowanceMapper.updateEntityFromDTO(requestDTO, existing);
        return fixedAllowanceMapper.toResponseDTO(fixedAllowanceRepository.save(existing));
    }

    @Override
    public void deleteFixedAllowance(Long id) {
        FixedAllowance fixedAllowance = fixedAllowanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedAllowance", "id", id));
        fixedAllowanceRepository.delete(fixedAllowance);
    }
}
