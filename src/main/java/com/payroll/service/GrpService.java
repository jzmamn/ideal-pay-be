package com.payroll.service;

import com.payroll.dto.request.GrpRequestDTO;
import com.payroll.dto.response.GrpResponseDTO;

import java.util.List;

public interface GrpService {

    List<GrpResponseDTO> getAllGroups(boolean showDefaultRow, String isActive);

    GrpResponseDTO getGroupById(Long id);

    GrpResponseDTO createGroup(GrpRequestDTO requestDTO);

    GrpResponseDTO updateGroup(Long id, GrpRequestDTO requestDTO);

    void deleteGroup(Long id);
}
