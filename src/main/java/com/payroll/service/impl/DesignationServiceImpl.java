package com.payroll.service.impl;

import com.payroll.dto.request.DesignationRequestDTO;
import com.payroll.dto.response.DesignationResponseDTO;
import com.payroll.entity.Designation;
import com.payroll.entity.Usr;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.DesignationMapper;
import com.payroll.repository.DesignationRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.DesignationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DesignationServiceImpl implements DesignationService {

    private final DesignationRepository designationRepository;
    private final UsrRepository usrRepository;
    private final DesignationMapper designationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DesignationResponseDTO> getAllDesignations(boolean showDefaultRow, String isActive) {
        if (!isActive.equals("true") && !isActive.equals("false") && !isActive.equals("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<Designation> records = "all".equals(isActive)
                ? designationRepository.findAll(sort)
                : designationRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(designationMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DesignationResponseDTO getDesignationById(Long id) {
        Designation designation = designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", id));
        return designationMapper.toResponseDTO(designation);
    }

    @Override
    public DesignationResponseDTO createDesignation(DesignationRequestDTO requestDTO) {
        Designation entity = designationMapper.toEntity(requestDTO);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        // Auto-generate code as DSG_<id>
        Designation saved = designationRepository.save(entity);
        saved.setCode("DSG_" + saved.getId());
        return designationMapper.toResponseDTO(designationRepository.save(saved));
    }

    @Override
    public DesignationResponseDTO updateDesignation(Long id, DesignationRequestDTO requestDTO) {
        Designation existing = designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", id));
        designationMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return designationMapper.toResponseDTO(designationRepository.save(existing));
    }

    @Override
    public void deleteDesignation(Long id) {
        Designation designation = designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", id));
        designationRepository.delete(designation);
    }
}
