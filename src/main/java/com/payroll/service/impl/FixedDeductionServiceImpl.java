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
public class FixedDeductionServiceImpl implements FixedDeductionService {

    private final FixedDeductionRepository fixedDeductionRepository;
    private final UsrRepository usrRepository;
    private final FixedDeductionMapper fixedDeductionMapper;
    private final FormulaEngineService formulaEngineService;
    private final SystemSetupService systemSetupService;

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
        validateFormula(requestDTO.getFormula());
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
        validateFormula(requestDTO.getFormula());
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

        // double, not BigDecimal — MVEL's compiled evaluator throws ArithmeticException
        // ("Non-terminating decimal expansion") on BigDecimal division that doesn't
        // terminate exactly (e.g. basicSalary / workingDays). See PayrollContextBuilder.
        Map<String, Object> ctx = new HashMap<>(context);
        ctx.putIfAbsent("basicSalary",  0.0d);
        ctx.putIfAbsent("BASIC_SALARY", 0.0d);
        ctx.putIfAbsent("workingDays",  systemSetupService.getWorkingDays());
        ctx.putIfAbsent("WORKING_DAYS", systemSetupService.getWorkingDays());

        if (fixedDeduction.getFormula() != null
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

        log.debug("FixedDeduction [{}] formula not enabled", fixedDeduction.getCode());
        return FormulaEvaluateResponseDTO.builder()
                .expression("no formula")
                .context(sanitise(ctx))
                .build();
    }

    /**
     * Validates the deduction definition:
     * <ul>
     *   <li>When a formula is present, it must be syntactically valid.</li>
     * </ul>
     */
    private void validateFormula(String formula) {
        if (formula != null && !formula.isBlank()) {
            String error = formulaEngineService.validateExpression(formula);
            if (error != null) {
                throw new IllegalArgumentException("Invalid formula: " + error);
            }
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
