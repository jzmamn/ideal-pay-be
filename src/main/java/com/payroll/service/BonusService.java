package com.payroll.service;

import com.payroll.dto.request.BonusRequestDTO;
import com.payroll.dto.response.BonusResponseDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;

import java.util.List;
import java.util.Map;

public interface BonusService {

    List<BonusResponseDTO> getAllBonuses(boolean showDefaultRow, String isActive);

    BonusResponseDTO getBonusById(Long id);

    BonusResponseDTO createBonus(BonusRequestDTO requestDTO);

    BonusResponseDTO updateBonus(Long id, BonusRequestDTO requestDTO);

    void deleteBonus(Long id);

    /**
     * Evaluates the linked MVEL formula for this bonus definition.
     * Falls back to the configured fixed {@code amount} if the formula is blank.
     */
    FormulaEvaluateResponseDTO calculateAmount(Long id, Map<String, Object> context);
}
