package com.payroll.controller;

import com.payroll.dto.request.EmailTemplateRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.EmailTemplateResponseDTO;
import com.payroll.entity.EmailTemplate.TemplateType;
import com.payroll.service.EmailTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/email-template")
@RequiredArgsConstructor
public class EmailTemplateController {

    private final EmailTemplateService emailTemplateService;

    /** All templates (active + inactive). */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<EmailTemplateResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Templates fetched", emailTemplateService.getAll()));
    }

    /** Active templates only, optionally filtered by type. */
    @GetMapping("/active")
    public ResponseEntity<ApiResponseDTO<List<EmailTemplateResponseDTO>>> getActive(
            @RequestParam(required = false) TemplateType type) {
        List<EmailTemplateResponseDTO> list = type != null
                ? emailTemplateService.getActiveByType(type)
                : emailTemplateService.getAllActive();
        return ResponseEntity.ok(ApiResponseDTO.success("Active templates fetched", list));
    }

    /** Single template by ID. */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmailTemplateResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Template fetched", emailTemplateService.getById(id)));
    }

    /** Create a new template. */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<EmailTemplateResponseDTO>> create(
            @Valid @RequestBody EmailTemplateRequestDTO dto,
            @RequestParam Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(
                "Template created", emailTemplateService.create(dto, userId)));
    }

    /** Update an existing template. */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmailTemplateResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody EmailTemplateRequestDTO dto,
            @RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Template updated", emailTemplateService.update(id, dto, userId)));
    }

    /** Delete a template. */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable Long id) {
        emailTemplateService.delete(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Template deleted", null));
    }

    /** All templates assigned to a specific SMTP config. */
    @GetMapping("/by-config/{configId}")
    public ResponseEntity<ApiResponseDTO<List<EmailTemplateResponseDTO>>> getByConfig(
            @PathVariable Long configId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Templates fetched", emailTemplateService.getByConfigId(configId)));
    }
}
