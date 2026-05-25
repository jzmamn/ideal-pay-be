package com.payroll.service.impl;

import com.payroll.dto.request.UserGroupRequestDTO;
import com.payroll.dto.response.UserGroupResponseDTO;
import com.payroll.entity.UserGroup;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.UserGroupMapper;
import com.payroll.repository.GrpRepository;
import com.payroll.repository.UserGroupRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.UserGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserGroupServiceImpl implements UserGroupService {

    private final UserGroupRepository userGroupRepository;
    private final UserGroupMapper userGroupMapper;
    private final UsrRepository usrRepository;
    private final GrpRepository grpRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserGroupResponseDTO> getAllUserGroups(boolean showDefaultRow) {
        Sort sort = Sort.by("id").ascending();
        return userGroupRepository.findAll(sort).stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(userGroupMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserGroupResponseDTO> getUserGroupsByUserId(Long userId) {
        Sort sort = Sort.by("id").ascending();
        return userGroupRepository.findAllByUserId(userId, sort).stream()
                .map(userGroupMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserGroupResponseDTO getUserGroupById(Long id) {
        UserGroup entity = userGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserGroup", "id", id));
        return userGroupMapper.toResponseDTO(entity);
    }

    @Override
    public UserGroupResponseDTO createUserGroup(UserGroupRequestDTO requestDTO) {
        UserGroup entity = userGroupMapper.toEntity(requestDTO);
        setRelationships(entity, requestDTO);
        return userGroupMapper.toResponseDTO(userGroupRepository.save(entity));
    }

    @Override
    public UserGroupResponseDTO updateUserGroup(Long id, UserGroupRequestDTO requestDTO) {
        UserGroup existing = userGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserGroup", "id", id));
        userGroupMapper.updateEntityFromDTO(requestDTO, existing);
        updateRelationships(existing, requestDTO);
        return userGroupMapper.toResponseDTO(userGroupRepository.save(existing));
    }

    @Override
    public void deleteUserGroup(Long id) {
        UserGroup entity = userGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserGroup", "id", id));
        userGroupRepository.delete(entity);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setRelationships(UserGroup entity, UserGroupRequestDTO dto) {
        entity.setUser(usrRepository.getReferenceById(dto.getUserId()));
        entity.setGroup(grpRepository.getReferenceById(dto.getGrpId()));
        entity.setCreatedBy(usrRepository.getReferenceById(dto.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }

    private void updateRelationships(UserGroup entity, UserGroupRequestDTO dto) {
        if (dto.getUserId() != null)
            entity.setUser(usrRepository.getReferenceById(dto.getUserId()));
        if (dto.getGrpId() != null)
            entity.setGroup(grpRepository.getReferenceById(dto.getGrpId()));
        if (dto.getModifiedBy() != null)
            entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }
}
