package com.payroll.service;

import com.payroll.dto.request.UserRoleRequestDTO;
import com.payroll.dto.response.UserRoleResponseDTO;

import java.util.List;

public interface UserRoleService {

    List<UserRoleResponseDTO> getAllUserRoles(boolean showDefaultRow, String isActive);

    UserRoleResponseDTO getUserRoleById(Long id);

    UserRoleResponseDTO createUserRole(UserRoleRequestDTO requestDTO);

    UserRoleResponseDTO updateUserRole(Long id, UserRoleRequestDTO requestDTO);

    void deleteUserRole(Long id);
}
