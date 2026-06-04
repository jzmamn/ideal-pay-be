package com.payroll.controller;

import com.payroll.dto.request.BankTransferTemplateRequestDTO;
import com.payroll.dto.request.MarkTransferredRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.BankTransferRowDTO;
import com.payroll.dto.response.BankTransferTemplateResponseDTO;
import com.payroll.service.BankTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/bank-transfer")
@RequiredArgsConstructor
public class BankTransferController {

    private final BankTransferService bankTransferService;

    // ── Templates ─────────────────────────────────────────────────────────────

    @GetMapping("/template")
    public ResponseEntity<ApiResponseDTO<List<BankTransferTemplateResponseDTO>>> getAllTemplates() {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Templates fetched successfully",
                bankTransferService.getAllTemplates()));
    }

    @PostMapping("/template")
    public ResponseEntity<ApiResponseDTO<BankTransferTemplateResponseDTO>> createTemplate(
            @Valid @RequestBody BankTransferTemplateRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(
                "Template created successfully",
                bankTransferService.saveTemplate(request)));
    }

    @PutMapping("/template/{id}")
    public ResponseEntity<ApiResponseDTO<BankTransferTemplateResponseDTO>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody BankTransferTemplateRequestDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Template updated successfully",
                bankTransferService.updateTemplate(id, request)));
    }

    @DeleteMapping("/template/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteTemplate(@PathVariable Long id) {
        bankTransferService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Template deleted successfully", null));
    }

    // ── Transfer preview ──────────────────────────────────────────────────────

    @GetMapping("/preview")
    public ResponseEntity<ApiResponseDTO<List<BankTransferRowDTO>>> getPreview(
            @RequestParam String month,
            @RequestParam List<String> types) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Transfer preview fetched successfully",
                bankTransferService.getTransferPreview(month, types)));
    }

    // ── Mark transferred ──────────────────────────────────────────────────────

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponseDTO<Void>> markTransferred(
            @Valid @RequestBody MarkTransferredRequestDTO request) {
        bankTransferService.markTransferred(request);
        return ResponseEntity.ok(ApiResponseDTO.success("Marked as transferred successfully", null));
    }
}
