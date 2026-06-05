package com.payroll.service;

import com.payroll.dto.request.EmailTemplateRequestDTO;
import com.payroll.dto.response.EmailTemplateResponseDTO;
import com.payroll.entity.EmailTemplate.TemplateType;

import java.util.List;

public interface EmailTemplateService {

    List<EmailTemplateResponseDTO> getAll();

    List<EmailTemplateResponseDTO> getAllActive();

    List<EmailTemplateResponseDTO> getActiveByType(TemplateType type);

    EmailTemplateResponseDTO getById(Long id);

    EmailTemplateResponseDTO create(EmailTemplateRequestDTO dto, Long userId);

    EmailTemplateResponseDTO update(Long id, EmailTemplateRequestDTO dto, Long userId);

    void delete(Long id);

    /** All templates assigned to a specific SMTP config. */
    List<EmailTemplateResponseDTO> getByConfigId(Long configId);
}
