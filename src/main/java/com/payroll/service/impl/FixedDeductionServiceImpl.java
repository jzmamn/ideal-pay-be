package com.payroll.service.impl;

import com.payroll.dto.request.FixedDeductionRequestDTO;
import com.payroll.dto.response.FixedDeductionResponseDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.entity.FixedDeduction;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.FixedDeductionMapper;
import com.payroll.repository.FixedDeductionRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.FixedDeductionService;
import com.payroll.service.FormulaEngineService;
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
public class FixedDeductionServiceImpl implements FixedDeductionService {

    private final FixedDeductionRepository fixedDeductionRepository;
    private final UsrRepository usrRepository;
    private final FixedDeductionMapper fixedDeductionMapper;
    private final FormulaEngineService formulaEngineService;

    @Override
    @Transactional(readOnly = true)
    public List<FixedDeductionResponseDTO> getAllFixedDeductions(boolean showDefaultRow, String isActive) {
        if (!isActive.equalsIgnoreCase("true") && !isActive.equalsIgnoreCase("false") && !isActive.equalsIgnoreCase("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<FixedDeduction> records = "all".equalsIgnoreCase(isActive)
                ? fixedDeductionRepository.findAll(sort)
                : fixedDeductionRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(fixedDeductionMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FixedDeductionResponseDTO getFixedDeductionById(Long id) {
        FixedDeduction entity = fixedDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedDeduction", "id", id));
        return fixedDeductionMapper.toResponseDTO(entity);
    }

    @Override
    public FixedDeductionResponseDTO createFixedDeduction(FixedDeductionRequestDTO requestDTO) {
        validateFormula(requestDTO.getFormulaEnabled(), requestDTO.getFormula());
        FixedDeduction entity = fixedDeductionMapper.toEntity(requestDTO);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        FixedDeduction saved = fixedDeductionRepository.save(entity);
        saved.setCode("FD_" + saved.getId());
        return fixedDeductionMapper.toResponseDTO(fixedDeductionRepository.save(saved));
    }

    @Override
    public FixedDeductionResponseDTO updateFixedDeduction(Long id, FixedDeductionRequestDTO requestDTO) {
        FixedDeduction existing = fixedDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedDeduction", "id", id));
        validateFormula(requestDTO.getFormulaEnabled(), requestDTO.getFormula());
        fixedDeductionMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return fixedDeductionMapper.toResponseDTO(fixedDeductionRepository.save(existing));
    }

    @Override
    public void deleteFixedDeduction(Long id) {
        FixedDeduction entity = fixedDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedDeduction", "id", id));
        fixedDeductionRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public FormulaEvaluateResponseDTO calculateAmount(Long id, Map<String, Object> context) {
        FixedDeduction fixedDeduction = fixedDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedDeduction", "id", id));

        Map<String, Object> ctx = new HashMap<>(context);
        ctx.putIfAbsent("basicSalary", BigDecimal.ZERO);
        ctx.putIfAbsent("workingDays", 26);

        if (Boolean.TRUE.equals(fixedDeduction.getFormulaEnabled())
                && fixedDeduction.getFormula() != null
                && !fixedDeduction.getFormula().isBlank()) {
            String expression = fixedDeduction.getFormula();
            try {
                BigDecimal result = formulaEngineService.evaluate(expression, ctx);
                return FormulaEvaluateResponseDTO.builder()
                        .expression(expression)
                        .result(result)
                        .context(sanitise(ctx))
                        .build();
            } catch (Exception ex) {
                log.warn("Formula evaluation failed for fixed-deduction [{}]: {}", fixedDeduction.getCode(), ex.getMessage());
                return FormulaEvaluateResponseDTO.builder()
                        .expression(expression)
                        .context(sanitise(ctx))
                        .technicalError(ex.getMessage())
                        .userFriendlyError(formulaEngineService.toUserFriendlyMessage(ex))
                        .build();
            }
        }

        log.debug("FixedDeduction [{}] formula not enabled — no fixed amount configured", fixedDeduction.getCode());
        return FormulaEvaluateResponseDTO.builder()
                .expression("formula not enabled")
                .context(sanitise(ctx))
                .build();
    }

    /**
     * Validates the MVEL formula when formulaEnabled is true.
     * Throws {@link IllegalArgumentException} (→ HTTP 400) if:
     * <ul>
     *   <li>formulaEnabled is true but formula is blank/null</li>
     *   <li>formulaEnabled is true and the expression has a syntax error</li>
     * </ul>
     * When formulaEnabled is false or null, the formula field is ignored.
     */
    private void validateFormula(Boolean formulaEnabled, String formula) {
        if (!Boolean.TRUE.equals(formulaEnabled)) return;
        if (formula == null || formula.isBlank()) {
            throw new IllegalArgumentException(
                    "Formula expression is required when formulaEnabled is true");
        }
        String error = formulaEngineService.validateExpression(formula);
        if (error != null) {
            throw new IllegalArgumentException("Invalid formula: " + error);
        }
    }

    private Map<String, Object> sanitise(Map<String, Object> ctx) {
        Map<String, Object> safe = new HashMap<>();
        ctx.forEach((k, v) -> {
            if (v instanceof Number || v instanceof String || v instanceof Boolean || v == null) {
                safe.put(k, v);
            } else {
                safe.put(k, v.toString());
            }
        });
        return safe;
    }
}
