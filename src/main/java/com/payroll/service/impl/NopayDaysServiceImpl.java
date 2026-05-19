package com.payroll.service.impl;

import com.payroll.dto.request.NopayDaysRequestDTO;
import com.payroll.dto.response.NopayDaysResponseDTO;
import com.payroll.entity.NopayDays;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.NopayDaysMapper;
import com.payroll.repository.NopayDaysRepository;
import com.payroll.service.NopayDaysService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NopayDaysServiceImpl implements NopayDaysService {

    private final NopayDaysRepository nopayDaysRepository;
    private final NopayDaysMapper nopayDaysMapper;

    @Override
    @Transactional(readOnly = true)
    public List<NopayDaysResponseDTO> getAllNopayDays(boolean showDefaultRow, String isActive) {
        if (!isActive.equals("true") && !isActive.equals("false") && !isActive.equals("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<NopayDays> records = "all".equals(isActive)
                ? nopayDaysRepository.findAll(sort)
                : nopayDaysRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(nopayDaysMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NopayDaysResponseDTO getNopayDaysById(Long id) {
        NopayDays nopayDays = nopayDaysRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NopayDays", "id", id));
        return nopayDaysMapper.toResponseDTO(nopayDays);
    }

    @Override
    public NopayDaysResponseDTO createNopayDays(NopayDaysRequestDTO requestDTO) {
        if (nopayDaysRepository.existsByCodeIgnoreCase(requestDTO.getCode())) {
            throw new IllegalArgumentException(
                    "A nopay days entry with code '" + requestDTO.getCode() + "' already exists.");
        }
        NopayDays entity = nopayDaysMapper.toEntity(requestDTO);
        return nopayDaysMapper.toResponseDTO(nopayDaysRepository.save(entity));
    }

    @Override
    public NopayDaysResponseDTO updateNopayDays(Long id, NopayDaysRequestDTO requestDTO) {
        NopayDays existing = nopayDaysRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NopayDays", "id", id));
        nopayDaysMapper.updateEntityFromDTO(requestDTO, existing);
        return nopayDaysMapper.toResponseDTO(nopayDaysRepository.save(existing));
    }

    @Override
    public void deleteNopayDays(Long id) {
        NopayDays nopayDays = nopayDaysRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NopayDays", "id", id));
        nopayDaysRepository.delete(nopayDays);
    }
}
