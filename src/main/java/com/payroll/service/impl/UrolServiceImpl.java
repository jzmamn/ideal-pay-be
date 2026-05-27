package com.payroll.service.impl;

import com.payroll.dto.request.UrolRequestDTO;
import com.payroll.dto.response.UrolResponseDTO;
import com.payroll.entity.Urol;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.UrolMapper;
import com.payroll.repository.UrolRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.UrolService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UrolServiceImpl implements UrolService {

    private final UrolRepository urolRepository;
    private final UsrRepository usrRepository;
    private final UrolMapper urolMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UrolResponseDTO> getAllRoles(boolean showDefaultRow, String isActive) {
        if (!isActive.equalsIgnoreCase("true") && !isActive.equalsIgnoreCase("false") && !isActive.equalsIgnoreCase("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<Urol> records = "all".equals(isActive)
                ? urolRepository.findAll(sort)
                : urolRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(urolMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UrolResponseDTO getRoleById(Long id) {
        Urol entity = urolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        return urolMapper.toResponseDTO(entity);
    }

    @Override
    public UrolResponseDTO createRole(UrolRequestDTO requestDTO) {
        Urol entity = urolMapper.toEntity(requestDTO);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        Urol saved = urolRepository.save(entity);
        saved.setCode("UROL_" + saved.getId());
        return urolMapper.toResponseDTO(urolRepository.save(saved));
    }

    @Override
    public UrolResponseDTO updateRole(Long id, UrolRequestDTO requestDTO) {
        Urol existing = urolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        urolMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return urolMapper.toResponseDTO(urolRepository.save(existing));
    }

    @Override
    public void deleteRole(Long id) {
        Urol entity = urolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        urolRepository.delete(entity);
    }
}
