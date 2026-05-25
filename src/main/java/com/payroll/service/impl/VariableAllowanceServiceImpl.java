package com.payroll.service.impl;

import com.payroll.dto.request.VariableAllowanceRequestDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.dto.response.VariableAllowanceResponseDTO;
import com.payroll.entity.VariableAllowance;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.formula.PayrollContextBuilder;
import com.payroll.mapper.VariableAllowanceMapper;
import com.payroll.repository.VariableAllowanceRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.FormulaEngineService;
import com.payroll.service.VariableAllowanceService;
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
public class VariableAllowanceServiceImpl implements VariableAllowanceService {

    private final VariableAllowanceRepository variableAllowanceRepository;
    private final UsrRepository usrRepository;
    private final VariableAllowanceMapper variableAllowanceMapper;
    private final FormulaEngineService formulaEngineService;

    @Override
    @Transactional(readOnly = true)
    public List<VariableAllowanceResponseDTO> getAllVariableAllowances(boolean showDefaultRow, String isActive) {
        if (!isActive.equals("true") && !isActive.equals("false") && !isActive.equals("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<VariableAllowance> records = "all".equals(isActive)
                ? variableAllowanceRepository.findAll(sort)
                : variableAllowanceRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(variableAllowanceMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VariableAllowanceResponseDTO getVariableAllowanceById(Long id) {
        VariableAllowance entity = variableAllowanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VariableAllowance", "id", id));
        return variableAllowanceMapper.toResponseDTO(entity);
    }

    @Override
    public VariableAllowanceResponseDTO createVariableAllowance(VariableAllowanceRequestDTO requestDTO) {
        VariableAllowance entity = variableAllowanceMapper.toEntity(requestDTO);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        VariableAllowance saved = variableAllowanceRepository.save(entity);
        saved.setCode("VA_" + saved.getId());
        return variableAllowanceMapper.toResponseDTO(variableAllowanceRepository.save(saved));
    }

    @Override
    public VariableAllowanceResponseDTO updateVariableAllowance(Long id, VariableAllowanceRequestDTO requestDTO) {
        VariableAllowance existing = variableAllowanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VariableAllowance", "id", id));
        variableAllowanceMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return variableAllowanceMapper.toResponseDTO(variableAllowanceRepository.save(existing));
    }

    @Override
    public void deleteVariableAllowance(Long id) {
        VariableAllowance entity = variableAllowanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VariableAllowance", "id", id));
        variableAllowanceRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public FormulaEvaluateResponseDTO calculateAmount(Long id, Map<String, Object> context) {
        VariableAllowance entity = variableAllowanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VariableAllowance", "id", id));

        if (!Boolean.TRUE.equals(entity.getFormulaEnabled())) {
            throw new IllegalStateException(
                    "Formula is not enabled for variable allowance '" + entity.getCode() + "'.");
        }
        if (entity.getFormula() == null || entity.getFormula().isBlank()) {
            throw new IllegalStateException(
                    "Variable allowance '" + entity.getCode() + "' has no formula configured.");
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
            log.warn("VariableAllowance [{}] formula error: {}", entity.getCode(), ex.getMessage());
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
