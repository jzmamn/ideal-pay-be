package com.payroll.service.impl;

import com.payroll.dto.request.FixedAllowanceRequestDTO;
import com.payroll.dto.response.FixedAllowanceResponseDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.entity.FixedAllowance;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.FixedAllowanceMapper;
import com.payroll.repository.FixedAllowanceRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.FixedAllowanceService;
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
public class FixedAllowanceServiceImpl implements FixedAllowanceService {

    private final FixedAllowanceRepository fixedAllowanceRepository;
    private final UsrRepository usrRepository;
    private final FixedAllowanceMapper fixedAllowanceMapper;
    private final FormulaEngineService formulaEngineService;

    @Override
    @Transactional(readOnly = true)
    public List<FixedAllowanceResponseDTO> getAllFixedAllowances(boolean showDefaultRow, String isActive) {
        if (!isActive.equalsIgnoreCase("true") && !isActive.equalsIgnoreCase("false") && !isActive.equalsIgnoreCase("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<FixedAllowance> records = "all".equalsIgnoreCase(isActive)
                ? fixedAllowanceRepository.findAll(sort)
                : fixedAllowanceRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(fixedAllowanceMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FixedAllowanceResponseDTO getFixedAllowanceById(Long id) {
        FixedAllowance entity = fixedAllowanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedAllowance", "id", id));
        return fixedAllowanceMapper.toResponseDTO(entity);
    }

    @Override
    public FixedAllowanceResponseDTO createFixedAllowance(FixedAllowanceRequestDTO requestDTO) {
        validateFormula(requestDTO.getFormulaEnabled(), requestDTO.getFormula());
        FixedAllowance entity = fixedAllowanceMapper.toEntity(requestDTO);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        FixedAllowance saved = fixedAllowanceRepository.save(entity);
        saved.setCode("FA_" + saved.getId());
        return fixedAllowanceMapper.toResponseDTO(fixedAllowanceRepository.save(saved));
    }

    @Override
    public FixedAllowanceResponseDTO updateFixedAllowance(Long id, FixedAllowanceRequestDTO requestDTO) {
        FixedAllowance existing = fixedAllowanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedAllowance", "id", id));
        validateFormula(requestDTO.getFormulaEnabled(), requestDTO.getFormula());
        fixedAllowanceMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return fixedAllowanceMapper.toResponseDTO(fixedAllowanceRepository.save(existing));
    }

    @Override
    public void deleteFixedAllowance(Long id) {
        FixedAllowance entity = fixedAllowanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedAllowance", "id", id));
        fixedAllowanceRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public FormulaEvaluateResponseDTO calculateAmount(Long id, Map<String, Object> context) {
        FixedAllowance fixedAllowance = fixedAllowanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedAllowance", "id", id));

        Map<String, Object> ctx = new HashMap<>(context);
        ctx.putIfAbsent("basicSalary", BigDecimal.ZERO);
        ctx.putIfAbsent("workingDays", 26);

        if (Boolean.TRUE.equals(fixedAllowance.getFormulaEnabled())
                && fixedAllowance.getFormula() != null
                && !fixedAllowance.getFormula().isBlank()) {
            String expression = fixedAllowance.getFormula();
            try {
                BigDecimal result = formulaEngineService.evaluate(expression, ctx);
                return FormulaEvaluateResponseDTO.builder()
                        .expression(expression)
                        .result(result)
                        .context(sanitise(ctx))
                        .build();
            } catch (Exception ex) {
                log.warn("Formula evaluation failed for fixed-allowance [{}]: {}", fixedAllowance.getCode(), ex.getMessage());
                return FormulaEvaluateResponseDTO.builder()
                        .expression(expression)
                        .context(sanitise(ctx))
                        .technicalError(ex.getMessage())
                        .userFriendlyError(formulaEngineService.toUserFriendlyMessage(ex))
                        .build();
            }
        }

        log.debug("FixedAllowance [{}] formula not enabled — returning configured amount {}", fixedAllowance.getCode(), fixedAllowance.getAmount());
        return FormulaEvaluateResponseDTO.builder()
                .expression("configured amount")
                .result(fixedAllowance.getAmount())
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
