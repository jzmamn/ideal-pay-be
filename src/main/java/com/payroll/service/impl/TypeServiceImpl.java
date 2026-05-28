package com.payroll.service.impl;

import com.payroll.dto.request.TypeRequestDTO;
import com.payroll.dto.response.TypeResponseDTO;
import com.payroll.entity.Type;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.TypeMapper;
import com.payroll.repository.TypeRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.TypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TypeServiceImpl implements TypeService {

    private final TypeRepository typeRepository;
    private final UsrRepository usrRepository;
    private final TypeMapper typeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TypeResponseDTO> getAllTypes(boolean showDefaultRow, String isActive) {
        if (!isActive.equalsIgnoreCase("true") && !isActive.equalsIgnoreCase("false") && !isActive.equalsIgnoreCase("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<Type> records = "all".equals(isActive)
                ? typeRepository.findAll(sort)
                : typeRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(typeMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TypeResponseDTO getTypeById(Long id) {
        Type type = typeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type", "id", id));
        return typeMapper.toResponseDTO(type);
    }

    @Override
    public TypeResponseDTO createType(TypeRequestDTO requestDTO) {
        Type entity = typeMapper.toEntity(requestDTO);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        Type saved = typeRepository.save(entity);
        saved.setCode("TYP_" + saved.getId());
        return typeMapper.toResponseDTO(typeRepository.save(saved));
    }

    @Override
    public TypeResponseDTO updateType(Long id, TypeRequestDTO requestDTO) {
        Type existing = typeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type", "id", id));
        typeMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return typeMapper.toResponseDTO(typeRepository.save(existing));
    }

    @Override
    public void deleteType(Long id) {
        Type type = typeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type", "id", id));
        typeRepository.delete(type);
    }
}
