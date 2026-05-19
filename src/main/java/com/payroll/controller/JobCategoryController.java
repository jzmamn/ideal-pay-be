package com.payroll.controller;

import com.payroll.dto.request.JobCategoryRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.JobCategoryResponseDTO;
import com.payroll.service.JobCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/job-category")
@RequiredArgsConstructor
public class JobCategoryController {

    private final JobCategoryService jobCategoryService;

    // GET /payroll/job-category
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<JobCategoryResponseDTO>>> getAllJobCategories(@RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow, @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Job categories fetched successfully",
                jobCategoryService.getAllJobCategories(showDefaultRow, isActive)));
    }

    // GET /payroll/job-category/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<JobCategoryResponseDTO>> getJobCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Job category fetched successfully",
                jobCategoryService.getJobCategoryById(id)));
    }

    // POST /payroll/job-category
    @PostMapping
    public ResponseEntity<ApiResponseDTO<JobCategoryResponseDTO>> createJobCategory(
            @Valid @RequestBody JobCategoryRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Job category created successfully",
                        jobCategoryService.createJobCategory(requestDTO)));
    }

    // PUT /payroll/job-category/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<JobCategoryResponseDTO>> updateJobCategory(
            @PathVariable Long id,
            @Valid @RequestBody JobCategoryRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Job category updated successfully",
                jobCategoryService.updateJobCategory(id, requestDTO)));
    }

    // DELETE /payroll/job-category/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteJobCategory(@PathVariable Long id) {
        jobCategoryService.deleteJobCategory(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Job category deleted successfully", null));
    }
}
