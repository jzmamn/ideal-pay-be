package com.payroll.service.impl;

import com.payroll.dto.request.NopayRequestDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.dto.response.NopayResponseDTO;
import com.payroll.entity.Nopay;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.NopayMapper;
import com.payroll.repository.NopayRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.FormulaEngineService;
import com.payroll.service.NopayService;
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
public class NopayServiceImpl implements NopayService {

    private final NopayRepository   nopayRepository;
    private final UsrRepository     usrRepository;
    private final NopayMapper       nopayMapper;
    private final FormulaEngineService formulaEngineService;

    @Override
    @Transactional(readOnly = true)
    public List<NopayResponseDTO> getAllNopay(String isActive) {
        if (!isActive.equalsIgnoreCase("true")
                && !isActive.equalsIgnoreCase("false")
                && !isActive.equalsIgnoreCase("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<Nopay> records = "all".equalsIgnoreCase(isActive)
                ? nopayRepository.findAll(sort)
                : nopayRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return nopayMapper.toResponseDTOList(records);
    }

    @Override
    @Transactional(readOnly = true)
    public NopayResponseDTO getNopayById(Long id) {
        Nopay entity = nopayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nopay", "id", id));
        return nopayMapper.toResponseDTO(entity);
    }

    @Override
    public NopayResponseDTO createNopay(NopayRequestDTO requestDTO) {
        validateFormula(requestDTO.getFormula());
        Nopay entity = nopayMapper.toEntity(requestDTO);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        Nopay saved = nopayRepository.save(entity);
        saved.setCode("NP_" + saved.getId());
        return nopayMapper.toResponseDTO(nopayRepository.save(saved));
    }

    @Override
    public NopayResponseDTO updateNopay(Long id, NopayRequestDTO requestDTO) {
        Nopay existing = nopayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nopay", "id", id));
        validateFormula(requestDTO.getFormula());
        nopayMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return nopayMapper.toResponseDTO(nopayRepository.save(existing));
    }

    @Override
    public void deleteNopay(Long id) {
        Nopay entity = nopayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nopay", "id", id));
        nopayRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public FormulaEvaluateResponseDTO calculateAmount(Long id, Map<String, Object> context) {
        Nopay nopay = nopayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nopay", "id", id));

        Map<String, Object> ctx = new HashMap<>(context);
        ctx.putIfAbsent("basicSalary",  BigDecimal.ZERO);
        ctx.putIfAbsent("BASIC_SALARY", BigDecimal.ZERO);
        ctx.putIfAbsent("workingDays",  26);
        ctx.putIfAbsent("WORKING_DAYS", 26);
        ctx.putIfAbsent("nopayDays",    BigDecimal.ZERO);
        ctx.putIfAbsent("NOPAY_DAYS",   BigDecimal.ZERO);

        if (nopay.getFormula() != null && !nopay.getFormula().isBlank()) {
            String expression = nopay.getFormula();
            try {
                BigDecimal result = formulaEngineService.evaluate(expression, ctx);
                return FormulaEvaluateResponseDTO.builder()
                        .expression(expression)
                        .result(result)
                        .context(sanitise(ctx))
                        .build();
            } catch (Exception ex) {
                log.warn("Formula evaluation failed for nopay [{}]: {}", nopay.getCode(), ex.getMessage());
                return FormulaEvaluateResponseDTO.builder()
                        .expression(expression)
                        .context(sanitise(ctx))
                        .technicalError(ex.getMessage())
                        .userFriendlyError(formulaEngineService.toUserFriendlyMessage(ex))
                        .build();
            }
        }

        log.debug("Nopay [{}] has no formula configured — using default calculation", nopay.getCode());
        return FormulaEvaluateResponseDTO.builder()
                .expression("no formula")
                .context(sanitise(ctx))
                .build();
    }

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
