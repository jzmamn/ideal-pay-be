package com.payroll.service;

import com.payroll.dto.request.VariableAllowanceRequestDTO;
import com.payroll.dto.response.VariableAllowanceResponseDTO;

import java.util.List;

public interface VariableAllowanceService {

    List<VariableAllowanceResponseDTO> getAllVariableAllowances(boolean showDefaultRow, String isActive);

    VariableAllowanceResponseDTO getVariableAllowanceById(Long id);

    VariableAllowanceResponseDTO createVariableAllowance(VariableAllowanceRequestDTO requestDTO);

    VariableAllowanceResponseDTO updateVariableAllowance(Long id, VariableAllowanceRequestDTO requestDTO);

    void deleteVariableAllowance(Long id);
}
