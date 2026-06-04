package com.payroll.controller;

import com.payroll.dto.request.EmailConfigRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.EmailConfigResponseDTO;
import com.payroll.service.EmailConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payroll/email-config")
@RequiredArgsConstructor
public class EmailConfigController {

    private final EmailConfigService emailConfigService;

    /** Get active email configuration (password excluded). */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<EmailConfigResponseDTO>> get() {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Email config fetched",
                emailConfigService.get()));
    }

    /** Create or replace the active email configuration. */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<EmailConfigResponseDTO>> save(
            @Valid @RequestBody EmailConfigRequestDTO dto,
            @RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Email config saved",
                emailConfigService.save(dto, userId)));
    }

    /** Test SMTP connectivity using the current active config. */
    @PostMapping("/test")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> testConnection() {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Test complete",
                emailConfigService.testConnection()));
    }
}
