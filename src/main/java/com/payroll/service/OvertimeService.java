package com.payroll.service;

import com.payroll.dto.request.OvertimeRequestDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.dto.response.OvertimeResponseDTO;

import java.util.List;
import java.util.Map;

public interface OvertimeService {

    List<OvertimeResponseDTO> getAllOvertimes(boolean showDefaultRow, String isActive);

    OvertimeResponseDTO getOvertimeById(Long id);

    OvertimeResponseDTO createOvertime(OvertimeRequestDTO requestDTO);

    OvertimeResponseDTO updateOvertime(Long id, OvertimeRequestDTO requestDTO);

    void deleteOvertime(Long id);

    /**
     * Computes the overtime amount for the given overtime type using its linked formula.
     * Falls back to the fixed {@code amount} field if no formula is configured.
     *
     * @param overtimeId ID of the Overtime record
     * @param context    payroll variable map (basicSalary, workingDays, otHours, otRate, …)
     * @return evaluation result with the computed amount, expression used, and context
     */
    FormulaEvaluateResponseDTO calculateAmount(Long overtimeId, Map<String, Object> context);
}
