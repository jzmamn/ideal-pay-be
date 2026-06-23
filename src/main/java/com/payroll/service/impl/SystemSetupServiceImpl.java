package com.payroll.service.impl;

import com.payroll.dto.request.SystemSetupUpdateRequestDTO;
import com.payroll.dto.response.SystemSetupResponseDTO;
import com.payroll.entity.SystemSetup;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.repository.SystemSetupRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.SystemSetupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SystemSetupServiceImpl implements SystemSetupService {

    private final SystemSetupRepository systemSetupRepository;
    private final UsrRepository usrRepository;

    private static final String CODE_WORKING_DAYS = "WORKING_DAYS";

    /**
     * Last-resort literal — only used if the WORKING_DAYS row itself is ever
     * deleted from system_setup. This is the one place that value is allowed
     * to be hardcoded; every other caller goes through {@link #getWorkingDays()}.
     */
    private static final int WORKING_DAYS_HARD_FALLBACK = 26;

    @Override
    @Transactional(readOnly = true)
    public List<SystemSetupResponseDTO> getAll() {
        return systemSetupRepository.findAllByOrderByCodeAsc().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SystemSetupResponseDTO getById(Long id) {
        return toDTO(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public SystemSetupResponseDTO getByCode(String code) {
        return systemSetupRepository.findByCode(code)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("SystemSetup", "code", code));
    }

    @Override
    public SystemSetupResponseDTO update(Long id, SystemSetupUpdateRequestDTO requestDTO) {
        SystemSetup setup = findOrThrow(id);
        setup.setValue(requestDTO.getValue());
        setup.setIsActive(requestDTO.getIsActive());
        setup.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        return toDTO(systemSetupRepository.save(setup));
    }

    @Override
    @Transactional(readOnly = true)
    public int getIntValue(String code, int fallback) {
        return systemSetupRepository.findByCode(code)
                .filter(setup -> Boolean.TRUE.equals(setup.getIsActive()))
                .map(SystemSetup::getValue)
                .map(value -> {
                    try {
                        return Integer.parseInt(value.trim());
                    } catch (NumberFormatException ex) {
                        return fallback;
                    }
                })
                .orElse(fallback);
    }

    @Override
    @Transactional(readOnly = true)
    public int getWorkingDays() {
        return getIntValue(CODE_WORKING_DAYS, WORKING_DAYS_HARD_FALLBACK);
    }

    private SystemSetup findOrThrow(Long id) {
        return systemSetupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SystemSetup", "id", id));
    }

    private SystemSetupResponseDTO toDTO(SystemSetup setup) {
        return SystemSetupResponseDTO.builder()
                .id(setup.getId())
                .code(setup.getCode())
                .name(setup.getName())
                .value(setup.getValue())
                .description(setup.getDescription())
                .isActive(setup.getIsActive())
                .effectiveFrom(setup.getEffectiveFrom())
                .effectiveTo(setup.getEffectiveTo())
                .createdById(setup.getCreatedBy().getId())
                .createdDate(setup.getCreatedDate())
                .modifiedById(setup.getModifiedBy().getId())
                .modifiedDate(setup.getModifiedDate())
                .build();
    }
}
