package com.payroll.service;

import com.payroll.dto.request.GradeRequestDTO;
import com.payroll.dto.response.GradeResponseDTO;

import java.util.List;

public interface GradeService {

    List<GradeResponseDTO> getAllGrades(boolean showDefaultRow, String isActive);

    GradeResponseDTO getGradeById(Long id);

    GradeResponseDTO createGrade(GradeRequestDTO requestDTO);

    GradeResponseDTO updateGrade(Long id, GradeRequestDTO requestDTO);

    void deleteGrade(Long id);
}
