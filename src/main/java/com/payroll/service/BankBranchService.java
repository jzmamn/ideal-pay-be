package com.payroll.service;

import com.payroll.dto.request.BankBranchRequestDTO;
import com.payroll.dto.response.BankBranchResponseDTO;

import java.util.List;

public interface BankBranchService {

    List<BankBranchResponseDTO> getAllBankBranches(String isActive);

    List<BankBranchResponseDTO> getBankBranchesByBankId(Long bankId);

    BankBranchResponseDTO getBankBranchById(Long id);

    BankBranchResponseDTO createBankBranch(BankBranchRequestDTO requestDTO);

    BankBranchResponseDTO updateBankBranch(Long id, BankBranchRequestDTO requestDTO);

    void deleteBankBranch(Long id);
}
