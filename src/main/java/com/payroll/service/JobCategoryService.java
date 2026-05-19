package com.payroll.service;

import com.payroll.dto.request.JobCategoryRequestDTO;
import com.payroll.dto.response.JobCategoryResponseDTO;

import java.util.List;

public interface JobCategoryService {

    List<JobCategoryResponseDTO> getAllJobCategories(boolean showDefaultRow, String isActive);

    JobCategoryResponseDTO getJobCategoryById(Long id);

    JobCategoryResponseDTO createJobCategory(JobCategoryRequestDTO requestDTO);

    JobCategoryResponseDTO updateJobCategory(Long id, JobCategoryRequestDTO requestDTO);

    void deleteJobCategory(Long id);
}
