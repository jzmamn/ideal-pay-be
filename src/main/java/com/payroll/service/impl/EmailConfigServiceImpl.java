package com.payroll.service.impl;

import com.payroll.dto.request.EmailConfigRequestDTO;
import com.payroll.dto.response.EmailConfigResponseDTO;
import com.payroll.entity.EmailConfig;
import com.payroll.entity.Usr;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.repository.EmailConfigRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.EmailConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailConfigServiceImpl implements EmailConfigService {

    private final EmailConfigRepository emailConfigRepo;
    private final UsrRepository         usrRepository;

    // ── Queries ───────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<EmailConfigResponseDTO> getAll() {
        return emailConfigRepo.findAllByOrderByIdDesc()
                .stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmailConfigResponseDTO getById(Long id) {
        return toDTO(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public EmailConfigResponseDTO getActive() {
        EmailConfig cfg = emailConfigRepo.findTopByIsActiveTrueOrderByIdDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No active email configuration found"));
        return toDTO(cfg);
    }

    // ── Commands ──────────────────────────────────────────────────────────

    @Override
    public EmailConfigResponseDTO create(EmailConfigRequestDTO dto, Long userId) {
        Usr usr = usrRepository.getReferenceById(userId);
        EmailConfig entity = EmailConfig.builder()
                .name(dto.getName())
                .host(dto.getHost())
                .port(dto.getPort())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .fromName(dto.getFromName())
                .fromAddress(dto.getFromAddress())
                .useTls(dto.getUseTls())
                .isActive(false)
                .createdBy(usr)
                .createdDate(LocalDateTime.now())
                .modifiedBy(usr)
                .modifiedDate(LocalDateTime.now())
                .build();
        return toDTO(emailConfigRepo.save(entity));
    }

    @Override
    public EmailConfigResponseDTO update(Long id, EmailConfigRequestDTO dto, Long userId) {
        EmailConfig entity = findOrThrow(id);
        Usr usr = usrRepository.getReferenceById(userId);
        entity.setName(dto.getName());
        entity.setHost(dto.getHost());
        entity.setPort(dto.getPort());
        entity.setUsername(dto.getUsername());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            entity.setPassword(dto.getPassword());
        }
        entity.setFromName(dto.getFromName());
        entity.setFromAddress(dto.getFromAddress());
        entity.setUseTls(dto.getUseTls());
        entity.setModifiedBy(usr);
        entity.setModifiedDate(LocalDateTime.now());
        return toDTO(emailConfigRepo.save(entity));
    }

    @Override
    public void delete(Long id) {
        EmailConfig cfg = findOrThrow(id);
        if (Boolean.TRUE.equals(cfg.getIsActive())) {
            throw new IllegalStateException("Cannot delete the active email configuration. Activate another first.");
        }
        emailConfigRepo.delete(cfg);
    }

    @Override
    public EmailConfigResponseDTO activate(Long id, Long userId) {
        EmailConfig cfg = findOrThrow(id);
        Usr usr = usrRepository.getReferenceById(userId);
        emailConfigRepo.deactivateAll();
        cfg.setIsActive(true);
        cfg.setModifiedBy(usr);
        cfg.setModifiedDate(LocalDateTime.now());
        return toDTO(emailConfigRepo.save(cfg));
    }

    @Override
    public EmailConfigResponseDTO deactivate(Long id, Long userId) {
        EmailConfig cfg = findOrThrow(id);
        Usr usr = usrRepository.getReferenceById(userId);
        cfg.setIsActive(false);
        cfg.setModifiedBy(usr);
        cfg.setModifiedDate(LocalDateTime.now());
        return toDTO(emailConfigRepo.save(cfg));
    }

    // ── Test connection ───────────────────────────────────────────────────

    @Override
    public Map<String, Object> testConnection() {
        EmailConfig cfg = emailConfigRepo.findTopByIsActiveTrueOrderByIdDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No active email configuration found"));
        return runTest(cfg);
    }

    @Override
    public Map<String, Object> testConnectionById(Long id) {
        return runTest(findOrThrow(id));
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private EmailConfig findOrThrow(Long id) {
        return emailConfigRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email configuration not found: " + id));
    }

    private Map<String, Object> runTest(EmailConfig cfg) {
        JavaMailSenderImpl sender = buildSender(cfg);
        Map<String, Object> result = new HashMap<>();
        try {
            sender.testConnection();
            result.put("success", true);
            result.put("message", "Connection to " + cfg.getHost() + ":" + cfg.getPort() + " successful.");
        } catch (Exception ex) {
            result.put("success", false);
            result.put("message", "Connection failed: " + ex.getMessage());
        }
        return result;
    }

    public static JavaMailSenderImpl buildSender(EmailConfig cfg) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(cfg.getHost());
        sender.setPort(cfg.getPort());
        sender.setUsername(cfg.getUsername());
        sender.setPassword(cfg.getPassword());

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        if (Boolean.TRUE.equals(cfg.getUseTls())) {
            props.put("mail.smtp.starttls.enable", "true");
        }
        return sender;
    }

    private EmailConfigResponseDTO toDTO(EmailConfig cfg) {
        return EmailConfigResponseDTO.builder()
                .id(cfg.getId())
                .name(cfg.getName())
                .host(cfg.getHost())
                .port(cfg.getPort())
                .username(cfg.getUsername())
                .fromName(cfg.getFromName())
                .fromAddress(cfg.getFromAddress())
                .useTls(cfg.getUseTls())
                .isActive(cfg.getIsActive())
                .build();
    }
}
