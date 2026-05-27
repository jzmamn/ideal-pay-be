package com.payroll.service.impl;

import com.payroll.dto.request.NopayDaysRequestDTO;
import com.payroll.dto.response.NopayDaysResponseDTO;
import com.payroll.entity.NopayDays;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.NopayDaysMapper;
import com.payroll.repository.NopayDaysRepository;
import com.payroll.repository.UsrRepository;
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
    private final UsrRepository usrRepository;
    private final NopayDaysMapper nopayDaysMapper;

    @Override
    @Transactional(readOnly = true)
    public List<NopayDaysResponseDTO> getAllNopayDays(boolean showDefaultRow, String isActive) {
        if (!isActive.equalsIgnoreCase("true") && !isActive.equalsIgnoreCase("false") && !isActive.equalsIgnoreCase("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<NopayDays> records = "all".equalsIgnoreCase(isActive)
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
        NopayDays entity = nopayDaysMapper.toEntity(requestDTO);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        NopayDays saved = nopayDaysRepository.save(entity);
        saved.setCode("NPD_" + saved.getId());
        return nopayDaysMapper.toResponseDTO(nopayDaysRepository.save(saved));
    }

    @Override
    public NopayDaysResponseDTO updateNopayDays(Long id, NopayDaysRequestDTO requestDTO) {
        NopayDays existing = nopayDaysRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NopayDays", "id", id));
        nopayDaysMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return nopayDaysMapper.toResponseDTO(nopayDaysRepository.save(existing));
    }

    @Override
    public void deleteNopayDays(Long id) {
        NopayDays nopayDays = nopayDaysRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NopayDays", "id", id));
        nopayDaysRepository.delete(nopayDays);
    }
}
