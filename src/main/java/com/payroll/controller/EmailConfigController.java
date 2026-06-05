package com.payroll.controller;

import com.payroll.dto.request.EmailConfigRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.EmailConfigResponseDTO;
import com.payroll.service.EmailConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payroll/email-config")
@RequiredArgsConstructor
public class EmailConfigController {

    private final EmailConfigService emailConfigService;

    /** All configurations (newest first, passwords excluded). */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<EmailConfigResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Email configs fetched", emailConfigService.getAll()));
    }

    /** Single configuration by ID. */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmailConfigResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Email config fetched", emailConfigService.getById(id)));
    }

    /** Currently active configuration. */
    @GetMapping("/active")
    public ResponseEntity<ApiResponseDTO<EmailConfigResponseDTO>> getActive() {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Active email config fetched", emailConfigService.getActive()));
    }

    /** Create a new configuration (inactive by default). */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<EmailConfigResponseDTO>> create(
            @Valid @RequestBody EmailConfigRequestDTO dto,
            @RequestParam Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(
                "Email config created", emailConfigService.create(dto, userId)));
    }

    /** Update an existing configuration. */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmailConfigResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody EmailConfigRequestDTO dto,
            @RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Email config updated", emailConfigService.update(id, dto, userId)));
    }

    /** Delete a configuration (must not be active). */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable Long id) {
        emailConfigService.delete(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Email config deleted", null));
    }

    /** Set a configuration as the active one; deactivates all others. */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponseDTO<EmailConfigResponseDTO>> activate(
            @PathVariable Long id,
            @RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Email config activated", emailConfigService.activate(id, userId)));
    }

    /** Deactivate a configuration. */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponseDTO<EmailConfigResponseDTO>> deactivate(
            @PathVariable Long id,
            @RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Email config deactivated", emailConfigService.deactivate(id, userId)));
    }

    /** Test SMTP connectivity of the currently active config. */
    @PostMapping("/test")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> testConnection() {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Test complete", emailConfigService.testConnection()));
    }

    /** Test SMTP connectivity of a specific config by ID. */
    @PostMapping("/{id}/test")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> testById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Test complete", emailConfigService.testConnectionById(id)));
    }
}
