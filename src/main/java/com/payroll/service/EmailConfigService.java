package com.payroll.service;

import com.payroll.dto.request.EmailConfigRequestDTO;
import com.payroll.dto.response.EmailConfigResponseDTO;

import java.util.Map;

public interface EmailConfigService {
    EmailConfigResponseDTO get();
    EmailConfigResponseDTO save(EmailConfigRequestDTO dto, Long userId);
    Map<String, Object> testConnection();
}
