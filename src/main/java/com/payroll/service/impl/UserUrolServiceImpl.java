package com.payroll.service.impl;

import com.payroll.dto.request.UserUrolRequestDTO;
import com.payroll.dto.response.UserUrolResponseDTO;
import com.payroll.entity.UserUrol;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.UserUrolMapper;
import com.payroll.repository.UrolRepository;
import com.payroll.repository.UserUrolRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.UserUrolService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserUrolServiceImpl implements UserUrolService {

    private final UserUrolRepository userUrolRepository;
    private final UserUrolMapper userUrolMapper;
    private final UsrRepository usrRepository;
    private final UrolRepository urolRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserUrolResponseDTO> getAllUserRoles(boolean showDefaultRow) {
        Sort sort = Sort.by("id").ascending();
        return userUrolRepository.findAll(sort).stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(userUrolMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserUrolResponseDTO> getUserRolesByUserId(Long userId) {
        Sort sort = Sort.by("id").ascending();
        return userUrolRepository.findAllByUserId(userId, sort).stream()
                .map(userUrolMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserUrolResponseDTO getUserRoleById(Long id) {
        UserUrol entity = userUrolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserUrol", "id", id));
        return userUrolMapper.toResponseDTO(entity);
    }

    @Override
    public UserUrolResponseDTO createUserRole(UserUrolRequestDTO requestDTO) {
        UserUrol entity = userUrolMapper.toEntity(requestDTO);
        setRelationships(entity, requestDTO);
        return userUrolMapper.toResponseDTO(userUrolRepository.save(entity));
    }

    @Override
    public UserUrolResponseDTO updateUserRole(Long id, UserUrolRequestDTO requestDTO) {
        UserUrol existing = userUrolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserUrol", "id", id));
        userUrolMapper.updateEntityFromDTO(requestDTO, existing);
        updateRelationships(existing, requestDTO);
        return userUrolMapper.toResponseDTO(userUrolRepository.save(existing));
    }

    @Override
    public void deleteUserRole(Long id) {
        UserUrol entity = userUrolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserUrol", "id", id));
        userUrolRepository.delete(entity);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setRelationships(UserUrol entity, UserUrolRequestDTO dto) {
        entity.setUser(usrRepository.getReferenceById(dto.getUserId()));
        entity.setUrol(urolRepository.getReferenceById(dto.getUrolId()));
        entity.setCreatedBy(usrRepository.getReferenceById(dto.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }

    private void updateRelationships(UserUrol entity, UserUrolRequestDTO dto) {
        if (dto.getUserId() != null)
            entity.setUser(usrRepository.getReferenceById(dto.getUserId()));
        if (dto.getUrolId() != null)
            entity.setUrol(urolRepository.getReferenceById(dto.getUrolId()));
        if (dto.getModifiedBy() != null)
            entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }
}
