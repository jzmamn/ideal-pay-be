package com.payroll.service.impl;

import com.payroll.dto.request.LateDeductionConfigRequestDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.dto.response.LateDeductionConfigResponseDTO;
import com.payroll.entity.LateDeductionConfig;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.LateDeductionConfigMapper;
import com.payroll.repository.LateDeductionConfigRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.FormulaEngineService;
import com.payroll.service.LateDeductionConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LateDeductionConfigServiceImpl implements LateDeductionConfigService {

    private final LateDeductionConfigRepository configRepository;
    private final UsrRepository                 usrRepository;
    private final LateDeductionConfigMapper     configMapper;
    private final FormulaEngineService          formulaEngineService;

    @Override
    @Transactional(readOnly = true)
    public List<LateDeductionConfigResponseDTO> getAll(String isActive) {
        Sort sort = Sort.by("id").ascending();
        List<LateDeductionConfig> records = "all".equalsIgnoreCase(isActive)
                ? configRepository.findAll(sort)
                : configRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        // id = -1 is the system default fallback row — never expose it to the UI
        return configMapper.toResponseDTOList(
                records.stream().filter(e -> e.getId() != -1L).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LateDeductionConfigResponseDTO getById(Long id) {
        return configMapper.toResponseDTO(findOrThrow(id));
    }

    @Override
    public LateDeductionConfigResponseDTO create(LateDeductionConfigRequestDTO requestDTO) {
        validateFormula(requestDTO.getFormula());
        LateDeductionConfig entity = configMapper.toEntity(requestDTO);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        LateDeductionConfig saved = configRepository.save(entity);
        saved.setCode("LD_" + saved.getId());
        return configMapper.toResponseDTO(configRepository.save(saved));
    }

    @Override
    public LateDeductionConfigResponseDTO update(Long id, LateDeductionConfigRequestDTO requestDTO) {
        LateDeductionConfig existing = findOrThrow(id);
        validateFormula(requestDTO.getFormula());
        configMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return configMapper.toResponseDTO(configRepository.save(existing));
    }

    @Override
    public void delete(Long id) {
        configRepository.delete(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public FormulaEvaluateResponseDTO calculateAmount(Long configId, Map<String, Object> context) {
        LateDeductionConfig config = findOrThrow(configId);

        Map<String, Object> ctx = new HashMap<>(context);
        ctx.putIfAbsent("basicSalary",           BigDecimal.ZERO);
        ctx.putIfAbsent("BASIC_SALARY",          BigDecimal.ZERO);
        ctx.putIfAbsent("workingDays",           config.getWorkingDays());
        ctx.putIfAbsent("WORKING_DAYS",          config.getWorkingDays());
        ctx.putIfAbsent("workingHoursPerDay",    config.getWorkingHoursPerDay());
        ctx.putIfAbsent("lateHours",             BigDecimal.ZERO);

        if (config.getFormula() != null && !config.getFormula().isBlank()) {
            try {
                BigDecimal result = formulaEngineService.evaluate(config.getFormula(), ctx);
                return FormulaEvaluateResponseDTO.builder()
                        .expression(config.getFormula())
                        .result(result)
                        .context(sanitise(ctx))
                        .build();
            } catch (Exception ex) {
                log.warn("Formula evaluation failed for LateDeductionConfig [{}]: {}",
                        config.getCode(), ex.getMessage());
                return FormulaEvaluateResponseDTO.builder()
                        .expression(config.getFormula())
                        .context(sanitise(ctx))
                        .technicalError(ex.getMessage())
                        .userFriendlyError(formulaEngineService.toUserFriendlyMessage(ex))
                        .build();
            }
        }

        // Default formula: basicSalary / (workingDays * workingHoursPerDay) * lateHours
        BigDecimal basicSalary   = toBD(ctx.get("basicSalary"));
        BigDecimal lateHours     = toBD(ctx.get("lateHours"));
        int        wDays         = config.getWorkingDays();
        int        wHours        = config.getWorkingHoursPerDay();
        BigDecimal defaultResult = (wDays * wHours == 0) ? BigDecimal.ZERO
                : basicSalary.divide(
                        BigDecimal.valueOf((long) wDays * wHours), 10, java.math.RoundingMode.HALF_UP)
                  .multiply(lateHours)
                  .setScale(2, java.math.RoundingMode.HALF_UP);

        String defaultExpr = "basicSalary / (" + wDays + " * " + wHours + ") * lateHours";
        return FormulaEvaluateResponseDTO.builder()
                .expression(defaultExpr)
                .result(defaultResult)
                .context(sanitise(ctx))
                .build();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private LateDeductionConfig findOrThrow(Long id) {
        return configRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LateDeductionConfig", "id", id));
    }

    private void validateFormula(String formula) {
        if (formula == null || formula.isBlank()) return;
        String error = formulaEngineService.validateExpression(formula);
        if (error != null) throw new IllegalArgumentException("Invalid formula: " + error);
    }

    private BigDecimal toBD(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        try { return new BigDecimal(value.toString()); }
        catch (Exception e) { return BigDecimal.ZERO; }
    }

    private Map<String, Object> sanitise(Map<String, Object> ctx) {
        Map<String, Object> safe = new HashMap<>();
        ctx.forEach((k, v) -> {
            if (v instanceof Number || v instanceof String || v instanceof Boolean || v == null)
                safe.put(k, v);
            else
                safe.put(k, v.toString());
        });
        return safe;
    }
}
