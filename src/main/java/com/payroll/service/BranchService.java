package com.payroll.service;

import com.payroll.dto.request.BranchRequestDTO;
import com.payroll.dto.response.BranchResponseDTO;

import java.util.List;

public interface BranchService {

    List<BranchResponseDTO> getAllBranches(boolean showDefaultRow, String isActive);

    BranchResponseDTO getBranchById(Long id);

    BranchResponseDTO createBranch(BranchRequestDTO requestDTO);

    BranchResponseDTO updateBranch(Long id, BranchRequestDTO requestDTO);

    void deleteBranch(Long id);
}
