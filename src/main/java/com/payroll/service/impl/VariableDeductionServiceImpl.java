package com.payroll.service.impl;

import com.payroll.dto.request.VariableDeductionRequestDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.dto.response.VariableDeductionResponseDTO;
import com.payroll.entity.VariableDeduction;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.formula.PayrollContextBuilder;
import com.payroll.mapper.VariableDeductionMapper;
import com.payroll.repository.VariableDeductionRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.FormulaEngineService;
import com.payroll.service.VariableDeductionService;
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
public class VariableDeductionServiceImpl implements VariableDeductionService {

    private final VariableDeductionRepository variableDeductionRepository;
    private final UsrRepository usrRepository;
    private final VariableDeductionMapper variableDeductionMapper;
    private final FormulaEngineService formulaEngineService;

    @Override
    @Transactional(readOnly = true)
    public List<VariableDeductionResponseDTO> getAllVariableDeductions(boolean showDefaultRow, String isActive) {
        if (!isActive.equals("true") && !isActive.equals("false") && !isActive.equals("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<VariableDeduction> records = "all".equals(isActive)
                ? variableDeductionRepository.findAll(sort)
                : variableDeductionRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(variableDeductionMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VariableDeductionResponseDTO getVariableDeductionById(Long id) {
        VariableDeduction entity = variableDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VariableDeduction", "id", id));
        return variableDeductionMapper.toResponseDTO(entity);
    }

    @Override
    public VariableDeductionResponseDTO createVariableDeduction(VariableDeductionRequestDTO requestDTO) {
        VariableDeduction entity = variableDeductionMapper.toEntity(requestDTO);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        VariableDeduction saved = variableDeductionRepository.save(entity);
        saved.setCode("VD_" + saved.getId());
        return variableDeductionMapper.toResponseDTO(variableDeductionRepository.save(saved));
    }

    @Override
    public VariableDeductionResponseDTO updateVariableDeduction(Long id, VariableDeductionRequestDTO requestDTO) {
        VariableDeduction existing = variableDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VariableDeduction", "id", id));
        variableDeductionMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return variableDeductionMapper.toResponseDTO(variableDeductionRepository.save(existing));
    }

    @Override
    public void deleteVariableDeduction(Long id) {
        VariableDeduction entity = variableDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VariableDeduction", "id", id));
        variableDeductionRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public FormulaEvaluateResponseDTO calculateAmount(Long id, Map<String, Object> context) {
        VariableDeduction entity = variableDeductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VariableDeduction", "id", id));

        if (!Boolean.TRUE.equals(entity.getFormulaEnabled())) {
            throw new IllegalStateException(
                    "Formula is not enabled for variable deduction '" + entity.getCode() + "'.");
        }
        if (entity.getFormula() == null || entity.getFormula().isBlank()) {
            throw new IllegalStateException(
                    "Variable deduction '" + entity.getCode() + "' has no formula configured.");
        }

        Map<String, Object> ctx = PayrollContextBuilder.builder()
                .customVariables(context)
                .build();

        String expression = entity.getFormula();
        try {
            BigDecimal result = formulaEngineService.evaluate(expression, ctx);
            return FormulaEvaluateResponseDTO.builder()
                    .expression(expression).result(result).context(sanitise(ctx)).build();
        } catch (Exception ex) {
            log.warn("VariableDeduction [{}] formula error: {}", entity.getCode(), ex.getMessage());
            return FormulaEvaluateResponseDTO.builder()
                    .expression(expression)
                    .context(sanitise(ctx))
                    .technicalError(ex.getMessage())
                    .userFriendlyError(formulaEngineService.toUserFriendlyMessage(ex))
                    .build();
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
