package com.payroll.service.impl;

import com.payroll.dto.request.LateDeductionConfigRequestDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.dto.response.LateDeductionConfigResponseDTO;
import com.payroll.entity.LateDeductionConfig;
import com.payroll.mapper.LateDeductionConfigMapper;
import com.payroll.repository.LateDeductionConfigRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.FormulaEngineService;
import com.payroll.service.LateDeductionConfigService;
import com.payroll.service.SystemSetupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LateDeductionConfigServiceImpl implements LateDeductionConfigService {

    private final LateDeductionConfigRepository configRepository;
    private final UsrRepository                 usrRepository;
    private final LateDeductionConfigMapper     configMapper;
    private final FormulaEngineService          formulaEngineService;
    private final SystemSetupService            systemSetupService;

    @Override
    @Transactional(readOnly = true)
    public LateDeductionConfigResponseDTO get() {
        return findSingleton()
                .map(configMapper::toResponseDTO)
                .orElse(null);
    }

    @Override
    public LateDeductionConfigResponseDTO save(LateDeductionConfigRequestDTO requestDTO) {
        validateFormula(requestDTO.getFormula());
        Optional<LateDeductionConfig> existing = findSingleton();

        if (existing.isPresent()) {
            LateDeductionConfig entity = existing.get();
            configMapper.updateEntityFromDTO(requestDTO, entity);
            entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
            return configMapper.toResponseDTO(configRepository.save(entity));
        }

        // First-time create
        LateDeductionConfig entity = configMapper.toEntity(requestDTO);
        entity.setCode("LD_DEFAULT");
        entity.setName(requestDTO.getName() != null && !requestDTO.getName().isBlank()
                ? requestDTO.getName() : "Late Deduction");
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        return configMapper.toResponseDTO(configRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public FormulaEvaluateResponseDTO calculateAmount(Map<String, Object> context) {
        LateDeductionConfig config = findSingleton().orElseGet(this::defaultConfig);

        Map<String, Object> ctx = new HashMap<>(context);
        ctx.putIfAbsent("basicSalary",        0.0d);
        ctx.putIfAbsent("BASIC_SALARY",       0.0d);
        ctx.putIfAbsent("workingDays",        config.getWorkingDays());
        ctx.putIfAbsent("WORKING_DAYS",       config.getWorkingDays());
        ctx.putIfAbsent("workingHoursPerDay", config.getWorkingHoursPerDay());
        ctx.putIfAbsent("lateHours",          0.0d);

        if (config.getFormula() != null && !config.getFormula().isBlank()) {
            try {
                BigDecimal result = formulaEngineService.evaluate(config.getFormula(), ctx);
                return FormulaEvaluateResponseDTO.builder()
                        .expression(config.getFormula())
                        .result(result)
                        .context(sanitise(ctx))
                        .build();
            } catch (Exception ex) {
                log.warn("Formula evaluation failed for late deduction config: {}", ex.getMessage());
                return FormulaEvaluateResponseDTO.builder()
                        .expression(config.getFormula())
                        .context(sanitise(ctx))
                        .technicalError(ex.getMessage())
                        .userFriendlyError(formulaEngineService.toUserFriendlyMessage(ex))
                        .build();
            }
        }

        // Default formula: basicSalary / (workingDays * workingHoursPerDay) * lateHours
        BigDecimal basicSalary = toBD(ctx.get("basicSalary"));
        BigDecimal lateHours   = toBD(ctx.get("lateHours"));
        int wDays  = config.getWorkingDays();
        int wHours = config.getWorkingHoursPerDay();
        BigDecimal defaultResult = (wDays * wHours == 0) ? BigDecimal.ZERO
                : basicSalary.divide(
                        BigDecimal.valueOf((long) wDays * wHours), 10, java.math.RoundingMode.HALF_UP)
                  .multiply(lateHours)
                  .setScale(2, java.math.RoundingMode.HALF_UP);

        return FormulaEvaluateResponseDTO.builder()
                .expression("basicSalary / (" + wDays + " * " + wHours + ") * lateHours")
                .result(defaultResult)
                .context(sanitise(ctx))
                .build();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Optional<LateDeductionConfig> findSingleton() {
        return configRepository.findTopByIdGreaterThanOrderByIdAsc(0L);
    }

    /** Transient default used for calculation when no config has been saved yet. */
    private LateDeductionConfig defaultConfig() {
        return LateDeductionConfig.builder()
                .workingDays(systemSetupService.getWorkingDays())
                .workingHoursPerDay(8)
                .build();
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
