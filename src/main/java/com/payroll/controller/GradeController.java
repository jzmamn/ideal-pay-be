package com.payroll.controller;

import com.payroll.dto.request.GradeRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.GradeResponseDTO;
import com.payroll.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/grade")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    // GET /payroll/grade
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<GradeResponseDTO>>> getAllGrades(@RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow, @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Grades fetched successfully",
                gradeService.getAllGrades(showDefaultRow, isActive)));
    }

    // GET /payroll/grade/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<GradeResponseDTO>> getGradeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Grade fetched successfully",
                gradeService.getGradeById(id)));
    }

    // POST /payroll/grade
    @PostMapping
    public ResponseEntity<ApiResponseDTO<GradeResponseDTO>> createGrade(
            @Valid @RequestBody GradeRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Grade created successfully",
                        gradeService.createGrade(requestDTO)));
    }

    // PUT /payroll/grade/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<GradeResponseDTO>> updateGrade(
            @PathVariable Long id,
            @Valid @RequestBody GradeRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Grade updated successfully",
                gradeService.updateGrade(id, requestDTO)));
    }

    // DELETE /payroll/grade/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Grade deleted successfully", null));
    }
}
