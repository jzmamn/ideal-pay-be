package com.payroll.service;

import com.payroll.dto.request.StatusRequestDTO;
import com.payroll.dto.response.StatusResponseDTO;
import java.util.List;

public interface StatusService {

    List<StatusResponseDTO> getAllStatuses(boolean showDefaultRow, String isActive);

    StatusResponseDTO getStatusById(Long id);

    StatusResponseDTO createStatus(StatusRequestDTO requestDTO);

    StatusResponseDTO updateStatus(Long id, StatusRequestDTO requestDTO);

    void deleteStatus(Long id);
}
