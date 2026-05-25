package com.payroll.service;

import com.payroll.dto.request.NopayDaysRequestDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.dto.response.NopayDaysResponseDTO;

import java.util.List;
import java.util.Map;

public interface NopayDaysService {

    List<NopayDaysResponseDTO> getAllNopayDays(boolean showDefaultRow, String isActive);

    NopayDaysResponseDTO getNopayDaysById(Long id);

    NopayDaysResponseDTO createNopayDays(NopayDaysRequestDTO requestDTO);

    NopayDaysResponseDTO updateNopayDays(Long id, NopayDaysRequestDTO requestDTO);

    void deleteNopayDays(Long id);

    /**
     * Computes the nopay deduction amount using the linked formula.
     * Falls back to the configured {@code days} value if no formula is linked.
     *
     * @param nopayDaysId ID of the NopayDays record
     * @param context     payroll variable map (basicSalary, workingDays, nopayDays, …)
     * @return evaluation result with the computed amount, expression used, and context
     */
    FormulaEvaluateResponseDTO calculateAmount(Long nopayDaysId, Map<String, Object> context);
}
