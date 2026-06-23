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
import com.payroll.service.SystemSetupService;
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
    private final SystemSetupService systemSetupService;

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
        validateFormula(requestDTO.getFormula());
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
        validateFormula(requestDTO.getFormula());
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

        // double, not BigDecimal — MVEL's compiled evaluator throws ArithmeticException
        // ("Non-terminating decimal expansion") on BigDecimal division that doesn't
        // terminate exactly (e.g. basicSalary / workingDays). See PayrollContextBuilder.
        Map<String, Object> ctx = new HashMap<>(context);
        ctx.putIfAbsent("basicSalary",  0.0d);
        ctx.putIfAbsent("BASIC_SALARY", 0.0d);
        ctx.putIfAbsent("workingDays",  systemSetupService.getWorkingDays());
        ctx.putIfAbsent("WORKING_DAYS", systemSetupService.getWorkingDays());

        if (fixedAllowance.getFormula() != null
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

        log.debug("FixedAllowance [{}] no formula configured", fixedAllowance.getCode());
        return FormulaEvaluateResponseDTO.builder()
                .expression("formula not enabled")
                .context(sanitise(ctx))
                .build();
    }

    /**
     * Validates the MVEL formula when one is present.
     * Throws {@link IllegalArgumentException} (→ HTTP 400) if the expression has a syntax error.
     * A blank/null formula is allowed — it simply means no formula is configured.
     */
    private void validateFormula(String formula) {
        if (formula == null || formula.isBlank()) return;
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
