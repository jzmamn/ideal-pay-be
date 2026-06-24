package com.payroll.service;

import com.payroll.dto.request.LateDeductionConfigRequestDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.dto.response.LateDeductionConfigResponseDTO;

import java.util.Map;

public interface LateDeductionConfigService {

    LateDeductionConfigResponseDTO get();

    LateDeductionConfigResponseDTO save(LateDeductionConfigRequestDTO requestDTO);

    FormulaEvaluateResponseDTO calculateAmount(Map<String, Object> context);
}
