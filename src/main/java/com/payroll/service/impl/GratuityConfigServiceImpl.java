package com.payroll.service.impl;

import com.payroll.dto.request.GratuityConfigRequest;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.dto.response.GratuityConfigResponse;
import com.payroll.entity.GratuityConfig;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.GratuityConfigMapper;
import com.payroll.repository.GratuityConfigRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.FormulaEngineService;
import com.payroll.service.GratuityConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class GratuityConfigServiceImpl implements GratuityConfigService {

    private final GratuityConfigRepository repo;
    private final UsrRepository            usrRepo;
    private final GratuityConfigMapper     mapper;
    private final FormulaEngineService     formulaEngine;

    private static final Sort ID_ASC = Sort.by("id").ascending();

    // ── Queries ────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<GratuityConfigResponse> getAll(String isActive) {
        List<GratuityConfig> rows = "all".equalsIgnoreCase(isActive)
                ? repo.findAll(ID_ASC)
                : repo.findAllByIsActive(Boolean.parseBoolean(isActive), ID_ASC);
        // id = -1 is system default — never expose to UI
        return rows.stream().filter(r -> r.getId() != -1L).map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GratuityConfigResponse getById(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public GratuityConfigResponse getActive() {
        return repo.findFirstByIsActiveTrueOrderByIdAsc()
                .filter(r -> r.getId() != -1L)
                .map(mapper::toResponse)
                .orElse(null);
    }

    // ── Mutations ──────────────────────────────────────────────────────────────

    @Override
    public GratuityConfigResponse create(GratuityConfigRequest request) {
        validateFormula(request.getFormula());
        GratuityConfig entity = GratuityConfig.builder()
                .name(request.getName())
                .description(request.getDescription())
                .formula(request.getFormula())
                .isActive(Boolean.TRUE.equals(request.getIsActive()))
                .createdBy(usrRepo.getReferenceById(request.getCreatedBy()))
                .modifiedBy(usrRepo.getReferenceById(request.getModifiedBy()))
                .build();
        GratuityConfig saved = repo.save(entity);
        saved.setCode("GT_" + saved.getId());
        return mapper.toResponse(repo.save(saved));
    }

    @Override
    public GratuityConfigResponse update(Long id, GratuityConfigRequest request) {
        GratuityConfig existing = findOrThrow(id);
        validateFormula(request.getFormula());
        mapper.updateFromRequest(request, existing);
        existing.setModifiedBy(usrRepo.getReferenceById(request.getModifiedBy()));
        return mapper.toResponse(repo.save(existing));
    }

    @Override
    public void delete(Long id) {
        repo.delete(findOrThrow(id));
    }

    // ── Formula calculation ────────────────────────────────────────────────────

    @Override
    public FormulaEvaluateResponseDTO calculateAmount(Long id, Map<String, Object> context) {
        GratuityConfig config = findOrThrow(id);

        BigDecimal basicSalary    = toBD(context.get("basicSalary"));
        BigDecimal yearsOfService = toBD(context.get("yearsOfService"));

        if (config.getFormula() != null && !config.getFormula().isBlank()) {
            try {
                BigDecimal result = formulaEngine.evaluate(config.getFormula(), context);
                return FormulaEvaluateResponseDTO.builder()
                        .expression(config.getFormula())
                        .result(result.setScale(2, RoundingMode.HALF_UP))
                        .context(sanitise(context))
                        .build();
            } catch (Exception ex) {
                return FormulaEvaluateResponseDTO.builder()
                        .expression(config.getFormula())
                        .technicalError(ex.getMessage())
                        .userFriendlyError(formulaEngine.toUserFriendlyMessage(ex))
                        .context(sanitise(context))
                        .build();
            }
        }

        // Default: basicSalary / 2 * yearsOfService
        BigDecimal result = basicSalary
                .divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_UP)
                .multiply(yearsOfService)
                .setScale(2, RoundingMode.HALF_UP);

        return FormulaEvaluateResponseDTO.builder()
                .expression("basicSalary / 2 * yearsOfService")
                .result(result)
                .context(sanitise(context))
                .build();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private GratuityConfig findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GratuityConfig", "id", id));
    }

    private void validateFormula(String formula) {
        if (formula == null || formula.isBlank()) return;
        String error = formulaEngine.validateExpression(formula);
        if (error != null) throw new IllegalArgumentException("Invalid formula: " + error);
    }

    private BigDecimal toBD(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal bd) return bd;
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private Map<String, Object> sanitise(Map<String, Object> ctx) {
        Map<String, Object> safe = new HashMap<>();
        ctx.forEach((k, v) -> safe.put(k,
                (v instanceof Number || v instanceof String || v instanceof Boolean || v == null)
                        ? v : v.toString()));
        return safe;
    }
}
