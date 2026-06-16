package com.payroll.service.impl;

import com.payroll.config.SecurityContextHelper;
import com.payroll.dto.request.BranchRequestDTO;
import com.payroll.dto.response.BranchResponseDTO;
import com.payroll.entity.Branch;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.BranchMapper;
import com.payroll.repository.BranchRepository;
import com.payroll.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;
    private final SecurityContextHelper securityContextHelper;

    @Override
    @Transactional(readOnly = true)
    public List<BranchResponseDTO> getAllBranches(boolean showDefaultRow, String isActive) {
        if (!isActive.equalsIgnoreCase("true") && !isActive.equalsIgnoreCase("false") && !isActive.equalsIgnoreCase("all")) {
            throw new IllegalArgumentException("Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<Branch> records = "all".equals(isActive)
                ? branchRepository.findAll(sort)
                : branchRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(branchMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BranchResponseDTO getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));
        return branchMapper.toResponseDTO(branch);
    }

    @Override
    public BranchResponseDTO createBranch(BranchRequestDTO requestDTO) {
        Branch entity = branchMapper.toEntity(requestDTO);
        var currentUser = securityContextHelper.getCurrentUser();
        entity.setCreatedBy(currentUser);
        entity.setModifiedBy(currentUser);
        Branch saved = branchRepository.save(entity);
        saved.setCode("BRN_" + saved.getId());
        return branchMapper.toResponseDTO(branchRepository.save(saved));
    }

    @Override
    public BranchResponseDTO updateBranch(Long id, BranchRequestDTO requestDTO) {
        Branch existing = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));
        branchMapper.updateEntityFromDTO(requestDTO, existing);
        existing.setModifiedBy(securityContextHelper.getCurrentUser());
        return branchMapper.toResponseDTO(branchRepository.save(existing));
    }

    @Override
    public void deleteBranch(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));
        branchRepository.delete(branch);
    }
}
