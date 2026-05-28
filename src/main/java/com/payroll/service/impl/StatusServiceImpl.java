package com.payroll.service.impl;

import com.payroll.dto.request.StatusRequestDTO;
import com.payroll.dto.response.StatusResponseDTO;
import com.payroll.entity.Status;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.StatusMapper;
import com.payroll.repository.StatusRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StatusServiceImpl implements StatusService {

    private final StatusRepository statusRepository;
    private final UsrRepository usrRepository;
    private final StatusMapper statusMapper;

    @Override
    @Transactional(readOnly = true)
    public List<StatusResponseDTO> getAllStatuses(boolean showDefaultRow, String isActive) {
        if (!isActive.equalsIgnoreCase("true") && !isActive.equalsIgnoreCase("false") && !isActive.equalsIgnoreCase("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<Status> records = "all".equalsIgnoreCase(isActive)
                ? statusRepository.findAll(sort)
                : statusRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(statusMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StatusResponseDTO getStatusById(Long id) {
        Status entity = statusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Status", "id", id));
        return statusMapper.toResponseDTO(entity);
    }

    @Override
    public StatusResponseDTO createStatus(StatusRequestDTO requestDTO) {
        Status entity = statusMapper.toEntity(requestDTO);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        Status saved = statusRepository.save(entity);
        saved.setCode("ST_" + saved.getId());
        return statusMapper.toResponseDTO(statusRepository.save(saved));
    }

    @Override
    public StatusResponseDTO updateStatus(Long id, StatusRequestDTO requestDTO) {
        Status existing = statusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Status", "id", id));
        statusMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return statusMapper.toResponseDTO(statusRepository.save(existing));
    }

    @Override
    public void deleteStatus(Long id) {
        Status entity = statusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Status", "id", id));
        statusRepository.delete(entity);
    }
}
