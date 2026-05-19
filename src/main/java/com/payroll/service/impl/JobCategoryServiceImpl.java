package com.payroll.service.impl;

import com.payroll.dto.request.JobCategoryRequestDTO;
import com.payroll.dto.response.JobCategoryResponseDTO;
import com.payroll.entity.JobCategory;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.JobCategoryMapper;
import com.payroll.repository.JobCategoryRepository;
import com.payroll.service.JobCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class JobCategoryServiceImpl implements JobCategoryService {

    private final JobCategoryRepository jobCategoryRepository;
    private final JobCategoryMapper jobCategoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<JobCategoryResponseDTO> getAllJobCategories(boolean showDefaultRow, String isActive) {
        if (!isActive.equals("true") && !isActive.equals("false") && !isActive.equals("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<JobCategory> records = "all".equals(isActive)
                ? jobCategoryRepository.findAll(sort)
                : jobCategoryRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(jobCategoryMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public JobCategoryResponseDTO getJobCategoryById(Long id) {
        JobCategory jobCategory = jobCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobCategory", "id", id));
        return jobCategoryMapper.toResponseDTO(jobCategory);
    }

    @Override
    public JobCategoryResponseDTO createJobCategory(JobCategoryRequestDTO requestDTO) {
        if (jobCategoryRepository.existsByCodeIgnoreCase(requestDTO.getCode())) {
            throw new IllegalArgumentException(
                    "A job category with code '" + requestDTO.getCode() + "' already exists.");
        }
        JobCategory entity = jobCategoryMapper.toEntity(requestDTO);
        return jobCategoryMapper.toResponseDTO(jobCategoryRepository.save(entity));
    }

    @Override
    public JobCategoryResponseDTO updateJobCategory(Long id, JobCategoryRequestDTO requestDTO) {
        JobCategory existing = jobCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobCategory", "id", id));
        jobCategoryMapper.updateEntityFromDTO(requestDTO, existing);
        return jobCategoryMapper.toResponseDTO(jobCategoryRepository.save(existing));
    }

    @Override
    public void deleteJobCategory(Long id) {
        JobCategory jobCategory = jobCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobCategory", "id", id));
        jobCategoryRepository.delete(jobCategory);
    }
}
