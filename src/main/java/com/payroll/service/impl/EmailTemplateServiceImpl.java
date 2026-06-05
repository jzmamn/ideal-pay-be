package com.payroll.service.impl;

import com.payroll.dto.request.EmailTemplateRequestDTO;
import com.payroll.dto.response.EmailTemplateResponseDTO;
import com.payroll.entity.EmailConfig;
import com.payroll.entity.EmailTemplate;
import com.payroll.entity.EmailTemplate.TemplateType;
import com.payroll.entity.Usr;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.repository.EmailConfigRepository;
import com.payroll.repository.EmailTemplateRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final EmailTemplateRepository templateRepo;
    private final EmailConfigRepository   emailConfigRepo;
    private final UsrRepository           usrRepository;

    // ── Queries ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<EmailTemplateResponseDTO> getAll() {
        return templateRepo.findAllByOrderByNameAsc()
                .stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailTemplateResponseDTO> getAllActive() {
        return templateRepo.findByIsActiveTrueOrderByNameAsc()
                .stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailTemplateResponseDTO> getActiveByType(TemplateType type) {
        return templateRepo.findByTemplateTypeAndIsActiveTrueOrderByNameAsc(type)
                .stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmailTemplateResponseDTO getById(Long id) {
        return toDTO(findOrThrow(id));
    }

    // ── Commands ──────────────────────────────────────────────────────────────

    @Override
    public EmailTemplateResponseDTO create(EmailTemplateRequestDTO dto, Long userId) {
        Usr usr = usrRepository.getReferenceById(userId);
        EmailConfig cfg = resolveConfig(dto.getEmailConfigId());
        EmailTemplate entity = EmailTemplate.builder()
                .name(dto.getName())
                .templateType(dto.getTemplateType())
                .emailConfig(cfg)
                .subject(dto.getSubject())
                .body(dto.getBody())
                .isActive(dto.getIsActive())
                .createdBy(usr)
                .modifiedBy(usr)
                .build();
        return toDTO(templateRepo.save(entity));
    }

    @Override
    public EmailTemplateResponseDTO update(Long id, EmailTemplateRequestDTO dto, Long userId) {
        EmailTemplate entity = findOrThrow(id);
        Usr usr = usrRepository.getReferenceById(userId);
        entity.setName(dto.getName());
        entity.setTemplateType(dto.getTemplateType());
        entity.setEmailConfig(resolveConfig(dto.getEmailConfigId()));
        entity.setSubject(dto.getSubject());
        entity.setBody(dto.getBody());
        entity.setIsActive(dto.getIsActive());
        entity.setModifiedBy(usr);
        return toDTO(templateRepo.save(entity));
    }

    @Override
    public void delete(Long id) {
        templateRepo.delete(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailTemplateResponseDTO> getByConfigId(Long configId) {
        return templateRepo.findByEmailConfigIdOrderByNameAsc(configId)
                .stream().map(this::toDTO).toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private EmailTemplate findOrThrow(Long id) {
        return templateRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email template not found: " + id));
    }

    /** Returns null if configId is null, otherwise loads the EmailConfig reference. */
    private EmailConfig resolveConfig(Long configId) {
        if (configId == null) return null;
        return emailConfigRepo.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Email config not found: " + configId));
    }

    private EmailTemplateResponseDTO toDTO(EmailTemplate e) {
        return EmailTemplateResponseDTO.builder()
                .id(e.getId())
                .name(e.getName())
                .templateType(e.getTemplateType())
                .emailConfigId(e.getEmailConfig() != null ? e.getEmailConfig().getId() : null)
                .emailConfigName(e.getEmailConfig() != null ? e.getEmailConfig().getName() : null)
                .subject(e.getSubject())
                .body(e.getBody())
                .isActive(e.getIsActive())
                .createdDate(e.getCreatedDate())
                .modifiedDate(e.getModifiedDate())
                .build();
    }
}
