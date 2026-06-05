package com.payroll.service;

import com.payroll.dto.request.EmailConfigRequestDTO;
import com.payroll.dto.response.EmailConfigResponseDTO;

import java.util.List;
import java.util.Map;

public interface EmailConfigService {

    /** Return all configurations (newest first). */
    List<EmailConfigResponseDTO> getAll();

    /** Return a single configuration by ID. */
    EmailConfigResponseDTO getById(Long id);

    /** Return the currently active configuration. */
    EmailConfigResponseDTO getActive();

    /** Create a new configuration (not active by default). */
    EmailConfigResponseDTO create(EmailConfigRequestDTO dto, Long userId);

    /** Update an existing configuration. */
    EmailConfigResponseDTO update(Long id, EmailConfigRequestDTO dto, Long userId);

    /** Delete a configuration. Throws if it is currently active. */
    void delete(Long id);

    /** Make this configuration the active one; deactivates all others. */
    EmailConfigResponseDTO activate(Long id, Long userId);

    /** Deactivate the currently active configuration. */
    EmailConfigResponseDTO deactivate(Long id, Long userId);

    /** Test SMTP connectivity using the currently active config. */
    Map<String, Object> testConnection();

    /** Test SMTP connectivity using a specific config by ID. */
    Map<String, Object> testConnectionById(Long id);
}
