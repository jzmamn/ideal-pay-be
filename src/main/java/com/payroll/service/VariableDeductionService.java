package com.payroll.service;

import com.payroll.dto.request.VariableDeductionRequestDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.dto.response.VariableDeductionResponseDTO;
import java.util.List;
import java.util.Map;

public interface VariableDeductionService {

    List<VariableDeductionResponseDTO> getAllVariableDeductions(boolean showDefaultRow, String isActive);

    VariableDeductionResponseDTO getVariableDeductionById(Long id);

    VariableDeductionResponseDTO createVariableDeduction(VariableDeductionRequestDTO requestDTO);

    VariableDeductionResponseDTO updateVariableDeduction(Long id, VariableDeductionRequestDTO requestDTO);

    void deleteVariableDeduction(Long id);

    /**
     * Evaluates the linked MVEL formula for this variable deduction type.
     * Throws IllegalStateException if no formula is configured.
     */
    FormulaEvaluateResponseDTO calculateAmount(Long id, Map<String, Object> context);
}
