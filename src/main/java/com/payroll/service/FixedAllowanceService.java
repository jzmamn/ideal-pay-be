package com.payroll.service;

import com.payroll.dto.request.FixedAllowanceRequestDTO;
import com.payroll.dto.response.FixedAllowanceResponseDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import java.util.List;
import java.util.Map;

public interface FixedAllowanceService {

    List<FixedAllowanceResponseDTO> getAllFixedAllowances(boolean showDefaultRow, String isActive);

    FixedAllowanceResponseDTO getFixedAllowanceById(Long id);

    FixedAllowanceResponseDTO createFixedAllowance(FixedAllowanceRequestDTO requestDTO);

    FixedAllowanceResponseDTO updateFixedAllowance(Long id, FixedAllowanceRequestDTO requestDTO);

    void deleteFixedAllowance(Long id);

    /**
     * Evaluates the linked MVEL formula for this fixed allowance type.
     * Falls back to the configured fixed {@code amount} if formulaEnabled is false or formula is blank.
     */
    FormulaEvaluateResponseDTO calculateAmount(Long id, Map<String, Object> context);
}
