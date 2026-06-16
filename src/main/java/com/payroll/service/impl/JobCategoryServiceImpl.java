package com.payroll.service.impl;

import com.payroll.config.SecurityContextHelper;
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
    private final SecurityContextHelper securityContextHelper;

    @Override
    @Transactional(readOnly = true)
    public List<JobCategoryResponseDTO> getAllJobCategories(boolean showDefaultRow, String isActive) {
        if (!isActive.equalsIgnoreCase("true") && !isActive.equalsIgnoreCase("false") && !isActive.equalsIgnoreCase("all")) {
            throw new IllegalArgumentException("Invalid value for isActive. Accepted values: true, false, all");
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
        JobCategory entity = jobCategoryMapper.toEntity(requestDTO);
        var currentUser = securityContextHelper.getCurrentUser();
        entity.setCreatedBy(currentUser);
        entity.setModifiedBy(currentUser);
        JobCategory saved = jobCategoryRepository.save(entity);
        saved.setCode("JBC_" + saved.getId());
        return jobCategoryMapper.toResponseDTO(jobCategoryRepository.save(saved));
    }

    @Override
    public JobCategoryResponseDTO updateJobCategory(Long id, JobCategoryRequestDTO requestDTO) {
        JobCategory existing = jobCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobCategory", "id", id));
        jobCategoryMapper.updateEntityFromDTO(requestDTO, existing);
        existing.setModifiedBy(securityContextHelper.getCurrentUser());
        return jobCategoryMapper.toResponseDTO(jobCategoryRepository.save(existing));
    }

    @Override
    public void deleteJobCategory(Long id) {
        JobCategory jobCategory = jobCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobCategory", "id", id));
        jobCategoryRepository.delete(jobCategory);
    }
}
