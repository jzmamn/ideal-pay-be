package com.payroll.service;

import com.payroll.dto.request.FixedDeductionRequestDTO;
import com.payroll.dto.response.FixedDeductionResponseDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import java.util.List;
import java.util.Map;

public interface FixedDeductionService {

    List<FixedDeductionResponseDTO> getAllFixedDeductions(boolean showDefaultRow, String isActive);

    FixedDeductionResponseDTO getFixedDeductionById(Long id);

    FixedDeductionResponseDTO createFixedDeduction(FixedDeductionRequestDTO requestDTO);

    FixedDeductionResponseDTO updateFixedDeduction(Long id, FixedDeductionRequestDTO requestDTO);

    void deleteFixedDeduction(Long id);

    /**
     * Evaluates the linked MVEL formula for this fixed deduction type.
     * Falls back to the configured fixed {@code amount} if formulaEnabled is false or formula is blank.
     */
    FormulaEvaluateResponseDTO calculateAmount(Long id, Map<String, Object> context);
}
