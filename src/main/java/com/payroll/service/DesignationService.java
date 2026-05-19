package com.payroll.service;

import com.payroll.dto.request.DesignationRequestDTO;
import com.payroll.dto.response.DesignationResponseDTO;

import java.util.List;

public interface DesignationService {

    List<DesignationResponseDTO> getAllDesignations(boolean showDefaultRow, String isActive);

    DesignationResponseDTO getDesignationById(Long id);

    DesignationResponseDTO createDesignation(DesignationRequestDTO requestDTO);

    DesignationResponseDTO updateDesignation(Long id, DesignationRequestDTO requestDTO);

    void deleteDesignation(Long id);
}
