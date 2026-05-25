package com.payroll.service.impl;

import com.payroll.dto.response.ActiveCodesResponseDTO;
import com.payroll.dto.response.CodeDropdownDTO;
import com.payroll.repository.*;
import com.payroll.service.CodeDropdownService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeDropdownServiceImpl implements CodeDropdownService {

    private final FixedAllowanceRepository    fixedAllowanceRepository;
    private final FixedDeductionRepository    fixedDeductionRepository;
    private final NopayDaysRepository         nopayDaysRepository;
    private final OvertimeRepository          overtimeRepository;
    private final VariableAllowanceRepository variableAllowanceRepository;
    private final VariableDeductionRepository variableDeductionRepository;

    private static final Sort BY_CODE = Sort.by("code").ascending();

    @Override
    public ActiveCodesResponseDTO getActiveCodes() {
        return ActiveCodesResponseDTO.builder()
                .fixedAllowances(
                        fixedAllowanceRepository.findAllByIsActive(true, BY_CODE).stream()
                                .map(e -> new CodeDropdownDTO(e.getId(), e.getCode(), e.getName()))
                                .toList())
                .fixedDeductions(
                        fixedDeductionRepository.findAllByIsActive(true, BY_CODE).stream()
                                .map(e -> new CodeDropdownDTO(e.getId(), e.getCode(), e.getName()))
                                .toList())
                .nopayDays(
                        nopayDaysRepository.findAllByIsActive(true, BY_CODE).stream()
                                .map(e -> new CodeDropdownDTO(e.getId(), e.getCode(), e.getName()))
                                .toList())
                .overtimes(
                        overtimeRepository.findAllByIsActive(true, BY_CODE).stream()
                                .map(e -> new CodeDropdownDTO(e.getId(), e.getCode(), e.getName()))
                                .toList())
                .variableAllowances(
                        variableAllowanceRepository.findAllByIsActive(true, BY_CODE).stream()
                                .map(e -> new CodeDropdownDTO(e.getId(), e.getCode(), e.getName()))
                                .toList())
                .variableDeductions(
                        variableDeductionRepository.findAllByIsActive(true, BY_CODE).stream()
                                .map(e -> new CodeDropdownDTO(e.getId(), e.getCode(), e.getName()))
                                .toList())
                .build();
    }
}
