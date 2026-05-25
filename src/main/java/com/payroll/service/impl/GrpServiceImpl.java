package com.payroll.service.impl;

import com.payroll.dto.request.GrpRequestDTO;
import com.payroll.dto.response.GrpResponseDTO;
import com.payroll.entity.Grp;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.GrpMapper;
import com.payroll.repository.GrpRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.GrpService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GrpServiceImpl implements GrpService {

    private final GrpRepository grpRepository;
    private final UsrRepository usrRepository;
    private final GrpMapper grpMapper;

    @Override
    @Transactional(readOnly = true)
    public List<GrpResponseDTO> getAllGroups(boolean showDefaultRow, String isActive) {
        if (!isActive.equals("true") && !isActive.equals("false") && !isActive.equals("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<Grp> records = "all".equals(isActive)
                ? grpRepository.findAll(sort)
                : grpRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(grpMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GrpResponseDTO getGroupById(Long id) {
        Grp entity = grpRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", id));
        return grpMapper.toResponseDTO(entity);
    }

    @Override
    public GrpResponseDTO createGroup(GrpRequestDTO requestDTO) {
        Grp entity = grpMapper.toEntity(requestDTO);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        Grp saved = grpRepository.save(entity);
        saved.setCode("GRP_" + saved.getId());
        return grpMapper.toResponseDTO(grpRepository.save(saved));
    }

    @Override
    public GrpResponseDTO updateGroup(Long id, GrpRequestDTO requestDTO) {
        Grp existing = grpRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", id));
        grpMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return grpMapper.toResponseDTO(grpRepository.save(existing));
    }

    @Override
    public void deleteGroup(Long id) {
        Grp entity = grpRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", id));
        grpRepository.delete(entity);
    }
}
