package com.payroll.service;

import com.payroll.dto.request.SystemSetupUpdateRequestDTO;
import com.payroll.dto.response.SystemSetupResponseDTO;

import java.util.List;

public interface SystemSetupService {
    List<SystemSetupResponseDTO> getAll();
    SystemSetupResponseDTO getById(Long id);
    SystemSetupResponseDTO getByCode(String code);
    SystemSetupResponseDTO update(Long id, SystemSetupUpdateRequestDTO requestDTO);

    /**
     * Resolves a system_setup value as an int, e.g. {@code getIntValue("WORKING_DAYS", 26)}.
     * Falls back to {@code fallback} when the code is missing, inactive, or not a valid integer.
     */
    int getIntValue(String code, int fallback);

    /**
     * The global default number of working/payable days per payroll period
     * (system_setup code {@code WORKING_DAYS}). This is the single source of
     * truth for "working days" defaults across the backend — callers should
     * use this instead of hardcoding a literal.
     */
    int getWorkingDays();
}
