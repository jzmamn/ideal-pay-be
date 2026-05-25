package com.payroll.service.impl;

import com.payroll.dto.request.OvertimeRequestDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.dto.response.OvertimeResponseDTO;
import com.payroll.entity.Overtime;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.OvertimeMapper;
import com.payroll.repository.OvertimeRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.FormulaEngineService;
import com.payroll.service.OvertimeService;
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
public class OvertimeServiceImpl implements OvertimeService {

    private final OvertimeRepository overtimeRepository;
    private final UsrRepository usrRepository;
    private final OvertimeMapper overtimeMapper;
    private final FormulaEngineService formulaEngineService;

    @Override
    @Transactional(readOnly = true)
    public List<OvertimeResponseDTO> getAllOvertimes(boolean showDefaultRow, String isActive) {
        if (!isActive.equals("true") && !isActive.equals("false") && !isActive.equals("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<Overtime> records = "all".equals(isActive)
                ? overtimeRepository.findAll(sort)
                : overtimeRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(overtimeMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OvertimeResponseDTO getOvertimeById(Long id) {
        Overtime overtime = overtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Overtime", "id", id));
        return overtimeMapper.toResponseDTO(overtime);
    }

    @Override
    public OvertimeResponseDTO createOvertime(OvertimeRequestDTO requestDTO) {
        Overtime entity = overtimeMapper.toEntity(requestDTO);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        Overtime saved = overtimeRepository.save(entity);
        saved.setCode("OT_" + saved.getId());
        return overtimeMapper.toResponseDTO(overtimeRepository.save(saved));
    }

    @Override
    public OvertimeResponseDTO updateOvertime(Long id, OvertimeRequestDTO requestDTO) {
        Overtime existing = overtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Overtime", "id", id));
        overtimeMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return overtimeMapper.toResponseDTO(overtimeRepository.save(existing));
    }

    @Override
    public void deleteOvertime(Long id) {
        Overtime overtime = overtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Overtime", "id", id));
        overtimeRepository.delete(overtime);
    }

    @Override
    @Transactional(readOnly = true)
    public FormulaEvaluateResponseDTO calculateAmount(Long overtimeId, Map<String, Object> context) {
        Overtime overtime = overtimeRepository.findById(overtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Overtime", "id", overtimeId));

        Map<String, Object> ctx = new HashMap<>(context);
        ctx.putIfAbsent("basicSalary", BigDecimal.ZERO);
        ctx.putIfAbsent("workingDays", 26);
        ctx.putIfAbsent("nopayDays",   0);
        ctx.putIfAbsent("otHours",     BigDecimal.ZERO);
        ctx.putIfAbsent("otRate",      BigDecimal.ONE);

        if (Boolean.TRUE.equals(overtime.getFormulaEnabled()) && overtime.getFormula() != null && !overtime.getFormula().isBlank()) {
            String expression = overtime.getFormula();
            try {
                BigDecimal result = formulaEngineService.evaluate(expression, ctx);
                return FormulaEvaluateResponseDTO.builder()
                        .expression(expression)
                        .result(result)
                        .context(sanitise(ctx))
                        .build();
            } catch (Exception ex) {
                log.warn("Formula evaluation failed for overtime [{}]: {}", overtime.getCode(), ex.getMessage());
                return FormulaEvaluateResponseDTO.builder()
                        .expression(expression)
                        .context(sanitise(ctx))
                        .technicalError(ex.getMessage())
                        .userFriendlyError(formulaEngineService.toUserFriendlyMessage(ex))
                        .build();
            }
        }

        log.debug("Overtime [{}] has no formula — returning fixed amount {}", overtime.getCode(), overtime.getAmount());
        return FormulaEvaluateResponseDTO.builder()
                .expression("fixed amount")
                .result(overtime.getAmount())
                .context(sanitise(ctx))
                .build();
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
