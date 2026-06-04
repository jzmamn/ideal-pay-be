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
import java.util.Map;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailConfigServiceImpl implements EmailConfigService {

    private final EmailConfigRepository emailConfigRepo;
    private final UsrRepository         usrRepository;

    // ── Get active config ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public EmailConfigResponseDTO get() {
        EmailConfig cfg = emailConfigRepo.findTopByIsActiveTrueOrderByIdDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No email configuration found"));
        return toDTO(cfg);
    }

    // ── Save (upsert) config ──────────────────────────────────────────────

    @Override
    public EmailConfigResponseDTO save(EmailConfigRequestDTO dto, Long userId) {
        Usr usr = usrRepository.getReferenceById(userId);

        // Deactivate existing active configs
        emailConfigRepo.findTopByIsActiveTrueOrderByIdDesc()
                .ifPresent(existing -> {
                    existing.setIsActive(false);
                    emailConfigRepo.save(existing);
                });

        EmailConfig entity = EmailConfig.builder()
                .host(dto.getHost())
                .port(dto.getPort())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .fromName(dto.getFromName())
                .fromAddress(dto.getFromAddress())
                .useTls(dto.getUseTls())
                .isActive(true)
                .createdBy(usr)
                .createdDate(LocalDateTime.now())
                .modifiedBy(usr)
                .modifiedDate(LocalDateTime.now())
                .build();

        return toDTO(emailConfigRepo.save(entity));
    }

    // ── Test connection ───────────────────────────────────────────────────

    @Override
    public Map<String, Object> testConnection() {
        EmailConfig cfg = emailConfigRepo.findTopByIsActiveTrueOrderByIdDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No email configuration found"));

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

    // ── Helpers ───────────────────────────────────────────────────────────

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
