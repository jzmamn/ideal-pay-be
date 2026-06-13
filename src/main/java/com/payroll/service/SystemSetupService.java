package com.payroll.service;

import com.payroll.dto.request.SystemSetupUpdateRequestDTO;
import com.payroll.dto.response.SystemSetupResponseDTO;

import java.util.List;

public interface SystemSetupService {
    List<SystemSetupResponseDTO> getAll();
    SystemSetupResponseDTO getById(Long id);
    SystemSetupResponseDTO getByCode(String code);
    SystemSetupResponseDTO update(Long id, SystemSetupUpdateRequestDTO requestDTO);
}
