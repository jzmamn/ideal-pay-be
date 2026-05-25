package com.payroll.service;

import com.payroll.dto.request.UserUrolRequestDTO;
import com.payroll.dto.response.UserUrolResponseDTO;

import java.util.List;

public interface UserUrolService {

    List<UserUrolResponseDTO> getAllUserRoles(boolean showDefaultRow);

    List<UserUrolResponseDTO> getUserRolesByUserId(Long userId);

    UserUrolResponseDTO getUserRoleById(Long id);

    UserUrolResponseDTO createUserRole(UserUrolRequestDTO requestDTO);

    UserUrolResponseDTO updateUserRole(Long id, UserUrolRequestDTO requestDTO);

    void deleteUserRole(Long id);
}
