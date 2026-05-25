package com.payroll.service;

import com.payroll.dto.request.VariableAllowanceRequestDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.dto.response.VariableAllowanceResponseDTO;
import java.util.List;
import java.util.Map;

public interface VariableAllowanceService {

    List<VariableAllowanceResponseDTO> getAllVariableAllowances(boolean showDefaultRow, String isActive);

    VariableAllowanceResponseDTO getVariableAllowanceById(Long id);

    VariableAllowanceResponseDTO createVariableAllowance(VariableAllowanceRequestDTO requestDTO);

    VariableAllowanceResponseDTO updateVariableAllowance(Long id, VariableAllowanceRequestDTO requestDTO);

    void deleteVariableAllowance(Long id);

    /**
     * Evaluates the linked MVEL formula for this variable allowance type.
     * Throws IllegalStateException if no formula is configured.
     */
    FormulaEvaluateResponseDTO calculateAmount(Long id, Map<String, Object> context);
}
