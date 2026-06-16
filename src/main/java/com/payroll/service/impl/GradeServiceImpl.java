package com.payroll.service.impl;

import com.payroll.config.SecurityContextHelper;
import com.payroll.dto.request.GradeRequestDTO;
import com.payroll.dto.response.GradeResponseDTO;
import com.payroll.entity.Grade;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.GradeMapper;
import com.payroll.repository.GradeRepository;
import com.payroll.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final GradeMapper gradeMapper;
    private final SecurityContextHelper securityContextHelper;

    @Override
    @Transactional(readOnly = true)
    public List<GradeResponseDTO> getAllGrades(boolean showDefaultRow, String isActive) {
        if (!isActive.equalsIgnoreCase("true") && !isActive.equalsIgnoreCase("false") && !isActive.equalsIgnoreCase("all")) {
            throw new IllegalArgumentException("Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<Grade> records = "all".equals(isActive)
                ? gradeRepository.findAll(sort)
                : gradeRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(gradeMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GradeResponseDTO getGradeById(Long id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade", "id", id));
        return gradeMapper.toResponseDTO(grade);
    }

    @Override
    public GradeResponseDTO createGrade(GradeRequestDTO requestDTO) {
        Grade entity = gradeMapper.toEntity(requestDTO);
        var currentUser = securityContextHelper.getCurrentUser();
        entity.setCreatedBy(currentUser);
        entity.setModifiedBy(currentUser);
        Grade saved = gradeRepository.save(entity);
        saved.setCode("GRD_" + saved.getId());
        return gradeMapper.toResponseDTO(gradeRepository.save(saved));
    }

    @Override
    public GradeResponseDTO updateGrade(Long id, GradeRequestDTO requestDTO) {
        Grade existing = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade", "id", id));
        gradeMapper.updateEntityFromDTO(requestDTO, existing);
        existing.setModifiedBy(securityContextHelper.getCurrentUser());
        return gradeMapper.toResponseDTO(gradeRepository.save(existing));
    }

    @Override
    public void deleteGrade(Long id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade", "id", id));
        gradeRepository.delete(grade);
    }
}
