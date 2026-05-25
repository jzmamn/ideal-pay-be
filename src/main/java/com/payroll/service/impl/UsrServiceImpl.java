package com.payroll.service.impl;

import com.payroll.dto.request.UsrRequestDTO;
import com.payroll.dto.response.UsrResponseDTO;
import com.payroll.entity.Usr;
import com.payroll.entity.UserRole;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.UsrMapper;
import com.payroll.repository.UserRoleRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.UsrService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UsrServiceImpl implements UsrService {

    private final UsrRepository usrRepository;
    private final UserRoleRepository userRoleRepository;
    private final UsrMapper usrMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UsrResponseDTO> getAllUsers(boolean showDefaultRow, String isActive) {
        if (!isActive.equals("true") && !isActive.equals("false") && !isActive.equals("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<Usr> records = "all".equals(isActive)
                ? usrRepository.findAll(sort)
                : usrRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(usrMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsrResponseDTO getUserById(Long id) {
        Usr usr = usrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return usrMapper.toResponseDTO(usr);
    }

    @Override
    public UsrResponseDTO createUser(UsrRequestDTO requestDTO) {
        if (usrRepository.existsByUserNameIgnoreCase(requestDTO.getUserName())) {
            throw new IllegalArgumentException(
                    "A user with username '" + requestDTO.getUserName() + "' already exists.");
        }
        if (usrRepository.existsByEmailIgnoreCase(requestDTO.getEmail())) {
            throw new IllegalArgumentException(
                    "A user with email '" + requestDTO.getEmail() + "' already exists.");
        }
        Usr entity = usrMapper.toEntity(requestDTO);

        // Hash the password before saving
        entity.setPassword(passwordEncoder.encode(requestDTO.getPassword()));

        // Resolve FK relationships using getReferenceById (no extra DB hit)
        entity.setRole(userRoleRepository.getReferenceById(requestDTO.getRoleId()));
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));

        // Auto-generate code as USER_<id> (mirrors GRP_<id> / UROL_<id> pattern)
        Usr saved = usrRepository.save(entity);
        saved.setCode("USER_" + saved.getId());
        return usrMapper.toResponseDTO(usrRepository.save(saved));
    }

    @Override
    public UsrResponseDTO updateUser(Long id, UsrRequestDTO requestDTO) {
        Usr existing = usrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // password is ignored in updateEntityFromDTO — use updatePassword() separately
        usrMapper.updateEntityFromDTO(requestDTO, existing);

        // Update FK relationships
        if (requestDTO.getRoleId() != null) {
            existing.setRole(userRoleRepository.getReferenceById(requestDTO.getRoleId()));
        }
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }

        return usrMapper.toResponseDTO(usrRepository.save(existing));
    }

    @Override
    public void updatePassword(Long id, String newPassword) {
        Usr existing = usrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        // Hash the new password before saving
        existing.setPassword(passwordEncoder.encode(newPassword));
        usrRepository.save(existing);
    }

    @Override
    public void deleteUser(Long id) {
        Usr usr = usrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        usrRepository.delete(usr);
    }
}
