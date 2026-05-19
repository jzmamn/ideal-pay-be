package com.payroll.service;

import com.payroll.dto.request.OvertimeRequestDTO;
import com.payroll.dto.response.OvertimeResponseDTO;

import java.util.List;

public interface OvertimeService {

    List<OvertimeResponseDTO> getAllOvertimes(boolean showDefaultRow, String isActive);

    OvertimeResponseDTO getOvertimeById(Long id);

    OvertimeResponseDTO createOvertime(OvertimeRequestDTO requestDTO);

    OvertimeResponseDTO updateOvertime(Long id, OvertimeRequestDTO requestDTO);

    void deleteOvertime(Long id);
}
