package com.payroll.service.impl;

import com.payroll.dto.request.BonusRequestDTO;
import com.payroll.dto.response.BonusResponseDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.entity.Bonus;
import com.payroll.enums.BonusCalculationMethod;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.BonusMapper;
import com.payroll.repository.BonusRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.BonusService;
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
public class BonusServiceImpl implements BonusService {

    private final BonusRepository bonusRepository;
    private final UsrRepository usrRepository;
    private final BonusMapper bonusMapper;
    private final FormulaEngineService formulaEngineService;

    @Override
    @Transactional(readOnly = true)
    public List<BonusResponseDTO> getAllBonuses(boolean showDefaultRow, String isActive) {
        if (!isActive.equalsIgnoreCase("true") && !isActive.equalsIgnoreCase("false") && !isActive.equalsIgnoreCase("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<Bonus> records = "all".equalsIgnoreCase(isActive)
                ? bonusRepository.findAll(sort)
                : bonusRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(bonusMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BonusResponseDTO getBonusById(Long id) {
        Bonus entity = bonusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bonus", "id", id));
        return bonusMapper.toResponseDTO(entity);
    }

    @Override
    public BonusResponseDTO createBonus(BonusRequestDTO requestDTO) {
        normaliseAndValidateCalculation(requestDTO);
        Bonus entity = bonusMapper.toEntity(requestDTO);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        Bonus saved = bonusRepository.save(entity);
        saved.setCode("BS_" + saved.getId());
        return bonusMapper.toResponseDTO(bonusRepository.save(saved));
    }

    @Override
    public BonusResponseDTO updateBonus(Long id, BonusRequestDTO requestDTO) {
        Bonus existing = bonusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bonus", "id", id));
        normaliseAndValidateCalculation(requestDTO);
        bonusMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return bonusMapper.toResponseDTO(bonusRepository.save(existing));
    }

    @Override
    public void deleteBonus(Long id) {
        Bonus entity = bonusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bonus", "id", id));
        bonusRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public FormulaEvaluateResponseDTO calculateAmount(Long id, Map<String, Object> context) {
        Bonus bonus = bonusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bonus", "id", id));

        Map<String, Object> ctx = new HashMap<>(context);
        ctx.putIfAbsent("basicSalary",  BigDecimal.ZERO);
        ctx.putIfAbsent("BASIC_SALARY", BigDecimal.ZERO);
        ctx.putIfAbsent("workingDays",  26);
        ctx.putIfAbsent("WORKING_DAYS", 26);

        if (bonus.getFormula() != null && !bonus.getFormula().isBlank()) {
            String expression = bonus.getFormula();
            try {
                BigDecimal result = formulaEngineService.evaluate(expression, ctx);
                return FormulaEvaluateResponseDTO.builder()
                        .expression(expression)
                        .result(result)
                        .context(sanitise(ctx))
                        .build();
            } catch (Exception ex) {
                log.warn("Formula evaluation failed for bonus [{}]: {}", bonus.getCode(), ex.getMessage());
                return FormulaEvaluateResponseDTO.builder()
                        .expression(expression)
                        .context(sanitise(ctx))
                        .technicalError(ex.getMessage())
                        .userFriendlyError(formulaEngineService.toUserFriendlyMessage(ex))
                        .build();
            }
        }

        log.debug("Bonus [{}] has no formula configured", bonus.getCode());
        return FormulaEvaluateResponseDTO.builder()
                .expression("no formula")
                .result(null)
                .context(sanitise(ctx))
                .build();
    }

    /**
     * Validates the MVEL formula. Bonus calculation is formula-only, so a
     * formula is always required.
     * Throws {@link IllegalArgumentException} (→ HTTP 400) if the formula is
     * blank/null or has a syntax error.
     */
    private void validateFormula(String formula) {
        if (formula == null || formula.isBlank()) {
            throw new IllegalArgumentException("Formula expression is required");
        }
        String error = formulaEngineService.validateExpression(formula);
        if (error != null) {
            throw new IllegalArgumentException("Invalid formula: " + error);
        }
    }

    private void normaliseAndValidateCalculation(BonusRequestDTO requestDTO) {
        requestDTO.setCalculationMethod(BonusCalculationMethod.FORMULA_BASED);
        validateFormula(requestDTO.getFormula());
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
