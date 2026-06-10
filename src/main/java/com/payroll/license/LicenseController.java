package com.payroll.license;

import com.payroll.dto.response.ApiResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/license")
@RequiredArgsConstructor
public class LicenseController {
    private final LicenseService service;

    @PostMapping("/import")
    public ResponseEntity<ApiResponseDTO<LicenseValidationResult>> importLicense(@Valid @RequestBody LicenseImportRequest request) {
        return ResponseEntity.ok(ApiResponseDTO.success("License imported successfully", service.importLicense(request)));
    }
    @GetMapping("/current")
    public ResponseEntity<ApiResponseDTO<LicenseValidationResult>> current() {
        return ResponseEntity.ok(ApiResponseDTO.success("Current license fetched", service.current()));
    }
    @GetMapping("/status")
    public ResponseEntity<ApiResponseDTO<LicenseValidationResult>> status() {
        return ResponseEntity.ok(ApiResponseDTO.success("License status fetched", service.validateCurrent()));
    }
    @PostMapping("/validate")
    public ResponseEntity<ApiResponseDTO<LicenseValidationResult>> validateLicense() {
        return ResponseEntity.ok(ApiResponseDTO.success("License validated", service.validateCurrent()));
    }
}
