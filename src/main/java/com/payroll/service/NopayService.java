package com.payroll.service;

import com.payroll.dto.request.NopayRequestDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.dto.response.NopayResponseDTO;

import java.util.List;
import java.util.Map;

public interface NopayService {

    List<NopayResponseDTO> getAllNopay(String isActive);

    NopayResponseDTO getNopayById(Long id);

    NopayResponseDTO createNopay(NopayRequestDTO requestDTO);

    NopayResponseDTO updateNopay(Long id, NopayRequestDTO requestDTO);

    void deleteNopay(Long id);

    /**
     * Evaluates the MVEL formula for this nopay type.
     * Falls back to the default formula {@code basicSalary / workingDays * nopayDays}
     * when formulaEnabled is false or formula is blank.
     */
    FormulaEvaluateResponseDTO calculateAmount(Long id, Map<String, Object> context);
}
