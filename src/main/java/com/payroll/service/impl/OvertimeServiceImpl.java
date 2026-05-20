package com.payroll.service.impl;

import com.payroll.dto.request.OvertimeRequestDTO;
import com.payroll.dto.response.OvertimeResponseDTO;
import com.payroll.entity.Overtime;
import com.payroll.entity.Usr;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.OvertimeMapper;
import com.payroll.repository.OvertimeRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.OvertimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OvertimeServiceImpl implements OvertimeService {

    private final OvertimeRepository overtimeRepository;
    private final UsrRepository usrRepository;
    private final OvertimeMapper overtimeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<OvertimeResponseDTO> getAllOvertimes(boolean showDefaultRow, String isActive) {
        if (!isActive.equals("true") && !isActive.equals("false") && !isActive.equals("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<Overtime> records = "all".equals(isActive)
                ? overtimeRepository.findAll(sort)
                : overtimeRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(overtimeMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OvertimeResponseDTO getOvertimeById(Long id) {
        Overtime overtime = overtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Overtime", "id", id));
        return overtimeMapper.toResponseDTO(overtime);
    }

    @Override
    public OvertimeResponseDTO createOvertime(OvertimeRequestDTO requestDTO) {
        if (overtimeRepository.existsByCodeIgnoreCase(requestDTO.getCode())) {
            throw new IllegalArgumentException(
                    "An overtime entry with code '" + requestDTO.getCode() + "' already exists.");
        }
        Overtime entity = overtimeMapper.toEntity(requestDTO);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        return overtimeMapper.toResponseDTO(overtimeRepository.save(entity));
    }

    @Override
    public OvertimeResponseDTO updateOvertime(Long id, OvertimeRequestDTO requestDTO) {
        Overtime existing = overtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Overtime", "id", id));
        overtimeMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return overtimeMapper.toResponseDTO(overtimeRepository.save(existing));
    }

    @Override
    public void deleteOvertime(Long id) {
        Overtime overtime = overtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Overtime", "id", id));
        overtimeRepository.delete(overtime);
    }
}
