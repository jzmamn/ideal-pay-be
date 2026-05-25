package com.payroll.service;

import com.payroll.dto.request.UrolRequestDTO;
import com.payroll.dto.response.UrolResponseDTO;

import java.util.List;

public interface UrolService {

    List<UrolResponseDTO> getAllRoles(boolean showDefaultRow, String isActive);

    UrolResponseDTO getRoleById(Long id);

    UrolResponseDTO createRole(UrolRequestDTO requestDTO);

    UrolResponseDTO updateRole(Long id, UrolRequestDTO requestDTO);

    void deleteRole(Long id);
}
