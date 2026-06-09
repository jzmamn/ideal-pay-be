package com.payroll.service;

import com.payroll.dto.request.LateDeductionConfigRequestDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.dto.response.LateDeductionConfigResponseDTO;

import java.util.List;
import java.util.Map;

public interface LateDeductionConfigService {

    List<LateDeductionConfigResponseDTO> getAll(String isActive);

    LateDeductionConfigResponseDTO getById(Long id);

    LateDeductionConfigResponseDTO create(LateDeductionConfigRequestDTO requestDTO);

    LateDeductionConfigResponseDTO update(Long id, LateDeductionConfigRequestDTO requestDTO);

    void delete(Long id);

    FormulaEvaluateResponseDTO calculateAmount(Long configId, Map<String, Object> context);
}
