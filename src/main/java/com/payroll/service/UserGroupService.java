package com.payroll.service;

import com.payroll.dto.request.UserGroupRequestDTO;
import com.payroll.dto.response.UserGroupResponseDTO;

import java.util.List;

public interface UserGroupService {

    List<UserGroupResponseDTO> getAllUserGroups(boolean showDefaultRow);

    List<UserGroupResponseDTO> getUserGroupsByUserId(Long userId);

    UserGroupResponseDTO getUserGroupById(Long id);

    UserGroupResponseDTO createUserGroup(UserGroupRequestDTO requestDTO);

    UserGroupResponseDTO updateUserGroup(Long id, UserGroupRequestDTO requestDTO);

    void deleteUserGroup(Long id);
}
