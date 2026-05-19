package com.payroll.service;

import com.payroll.dto.request.VariableDeductionRequestDTO;
import com.payroll.dto.response.VariableDeductionResponseDTO;

import java.util.List;

public interface VariableDeductionService {

    List<VariableDeductionResponseDTO> getAllVariableDeductions(boolean showDefaultRow, String isActive);

    VariableDeductionResponseDTO getVariableDeductionById(Long id);

    VariableDeductionResponseDTO createVariableDeduction(VariableDeductionRequestDTO requestDTO);

    VariableDeductionResponseDTO updateVariableDeduction(Long id, VariableDeductionRequestDTO requestDTO);

    void deleteVariableDeduction(Long id);
}
