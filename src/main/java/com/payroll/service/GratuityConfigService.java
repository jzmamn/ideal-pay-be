package com.payroll.service;

import com.payroll.dto.request.GratuityConfigRequest;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.dto.response.GratuityConfigResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface GratuityConfigService {

    List<GratuityConfigResponse> getAll(String isActive);

    GratuityConfigResponse getById(Long id);

    GratuityConfigResponse getActive();

    GratuityConfigResponse create(GratuityConfigRequest request);

    GratuityConfigResponse update(Long id, GratuityConfigRequest request);

    void delete(Long id);

    FormulaEvaluateResponseDTO calculateAmount(Long id, Map<String, Object> context);
}
