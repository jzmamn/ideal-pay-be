package com.payroll.service.impl;

import com.payroll.dto.request.DistrictRequestDTO;
import com.payroll.dto.response.DistrictResponseDTO;
import com.payroll.entity.District;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.DistrictMapper;
import com.payroll.repository.DistrictRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.DistrictService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DistrictServiceImpl implements DistrictService {

    private final DistrictRepository districtRepository;
    private final DistrictMapper districtMapper;
    private final UsrRepository usrRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DistrictResponseDTO> getAllDistricts(boolean showDefaultRow, String isActive) {
        if (!isActive.equalsIgnoreCase("true") && !isActive.equalsIgnoreCase("false") && !isActive.equalsIgnoreCase("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("name").ascending();
        List<District> records = "all".equals(isActive)
                ? districtRepository.findAll(sort)
                : districtRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(districtMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DistrictResponseDTO getDistrictById(Long id) {
        District district = districtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("District", "id", id));
        return districtMapper.toResponseDTO(district);
    }

    @Override
    public DistrictResponseDTO createDistrict(DistrictRequestDTO requestDTO) {
        if (districtRepository.existsByNameIgnoreCase(requestDTO.getName())) {
            throw new IllegalArgumentException(
                    "A district with name '" + requestDTO.getName() + "' already exists.");
        }
        District entity = districtMapper.toEntity(requestDTO);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        // Auto-generate code as DST_<id>
        District saved = districtRepository.save(entity);
        saved.setCode("DST_" + saved.getId());
        return districtMapper.toResponseDTO(districtRepository.save(saved));
    }

    @Override
    public DistrictResponseDTO updateDistrict(Long id, DistrictRequestDTO requestDTO) {
        District existing = districtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("District", "id", id));
        districtMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return districtMapper.toResponseDTO(districtRepository.save(existing));
    }

    @Override
    public void deleteDistrict(Long id) {
        District district = districtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("District", "id", id));
        districtRepository.delete(district);
    }
}
