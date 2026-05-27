package com.payroll.service.impl;

import com.payroll.dto.request.UserRoleRequestDTO;
import com.payroll.dto.response.UserRoleResponseDTO;
import com.payroll.entity.UserRole;
import com.payroll.entity.Usr;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.UserRoleMapper;
import com.payroll.repository.UserRoleRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Sort;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UsrRepository usrRepository;
    private final UserRoleMapper userRoleMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserRoleResponseDTO> getAllUserRoles(boolean showDefaultRow, String isActive) {
        if (!isActive.equalsIgnoreCase("true") && !isActive.equalsIgnoreCase("false") && !isActive.equalsIgnoreCase("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<UserRole> records = "all".equals(isActive)
                ? userRoleRepository.findAll(sort)
                : userRoleRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(userRoleMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserRoleResponseDTO getUserRoleById(Long id) {
        UserRole userRole = userRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserRole", "id", id));
        return userRoleMapper.toResponseDTO(userRole);
    }

    @Override
    public UserRoleResponseDTO createUserRole(UserRoleRequestDTO requestDTO) {
        if (userRoleRepository.existsByCodeIgnoreCase(requestDTO.getCode())) {
            throw new IllegalArgumentException(
                    "A user role with code '" + requestDTO.getCode() + "' already exists.");
        }
        UserRole entity = userRoleMapper.toEntity(requestDTO);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        return userRoleMapper.toResponseDTO(userRoleRepository.save(entity));
    }

    @Override
    public UserRoleResponseDTO updateUserRole(Long id, UserRoleRequestDTO requestDTO) {
        UserRole existing = userRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserRole", "id", id));
        userRoleMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return userRoleMapper.toResponseDTO(userRoleRepository.save(existing));
    }

    @Override
    public void deleteUserRole(Long id) {
        UserRole userRole = userRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserRole", "id", id));
        userRoleRepository.delete(userRole);
    }
}
