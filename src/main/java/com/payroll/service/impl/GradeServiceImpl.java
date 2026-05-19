package com.payroll.service.impl;

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

    @Override
    @Transactional(readOnly = true)
    public List<GradeResponseDTO> getAllGrades(boolean showDefaultRow, String isActive) {
        if (!isActive.equals("true") && !isActive.equals("false") && !isActive.equals("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
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
        if (gradeRepository.existsByCodeIgnoreCase(requestDTO.getCode())) {
            throw new IllegalArgumentException(
                    "A grade with code '" + requestDTO.getCode() + "' already exists.");
        }
        Grade entity = gradeMapper.toEntity(requestDTO);
        return gradeMapper.toResponseDTO(gradeRepository.save(entity));
    }

    @Override
    public GradeResponseDTO updateGrade(Long id, GradeRequestDTO requestDTO) {
        Grade existing = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade", "id", id));
        gradeMapper.updateEntityFromDTO(requestDTO, existing);
        return gradeMapper.toResponseDTO(gradeRepository.save(existing));
    }

    @Override
    public void deleteGrade(Long id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade", "id", id));
        gradeRepository.delete(grade);
    }
}
