package com.payroll.service;

import com.payroll.dto.request.UsrRequestDTO;
import com.payroll.dto.response.UsrResponseDTO;

import java.util.List;

public interface UsrService {

    List<UsrResponseDTO> getAllUsers(boolean showDefaultRow, String isActive);

    UsrResponseDTO getUserById(Long id);

    UsrResponseDTO createUser(UsrRequestDTO requestDTO);

    UsrResponseDTO updateUser(Long id, UsrRequestDTO requestDTO);

    void updatePassword(Long id, String newPassword);

    void deleteUser(Long id);
}
