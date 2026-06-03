package com.payroll.service;

import com.payroll.dto.request.BankRequestDTO;
import com.payroll.dto.response.BankResponseDTO;

import java.util.List;

public interface BankService {

    List<BankResponseDTO> getAllBanks(boolean showDefaultRow, String isActive);

    BankResponseDTO getBankById(Long id);

    BankResponseDTO createBank(BankRequestDTO requestDTO);

    BankResponseDTO updateBank(Long id, BankRequestDTO requestDTO);

    void deleteBank(Long id);
}
