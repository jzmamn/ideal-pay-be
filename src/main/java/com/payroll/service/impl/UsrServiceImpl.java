package com.payroll.service.impl;

import com.payroll.dto.request.UsrRequestDTO;
import com.payroll.dto.response.UsrResponseDTO;
import com.payroll.entity.Usr;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.UsrMapper;
import com.payroll.repository.UsrRepository;
import com.payroll.service.UsrService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Sort;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UsrServiceImpl implements UsrService {

    private final UsrRepository usrRepository;
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
        if (usrRepository.existsByCodeIgnoreCase(requestDTO.getCode())) {
            throw new IllegalArgumentException(
                    "A user with code '" + requestDTO.getCode() + "' already exists.");
        }
        Usr entity = usrMapper.toEntity(requestDTO);
        // Hash the password before saving
        entity.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        return usrMapper.toResponseDTO(usrRepository.save(entity));
    }

    @Override
    public UsrResponseDTO updateUser(Long id, UsrRequestDTO requestDTO) {
        Usr existing = usrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        // password is ignored in updateEntityFromDTO — use updatePassword() separately
        usrMapper.updateEntityFromDTO(requestDTO, existing);
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
