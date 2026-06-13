package com.payroll.controller;

import com.payroll.dto.request.SystemSetupUpdateRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.SystemSetupResponseDTO;
import com.payroll.service.SystemSetupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/system-setup")
@RequiredArgsConstructor
public class SystemSetupController {

    private final SystemSetupService systemSetupService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<SystemSetupResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "System setup values fetched successfully", systemSetupService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<SystemSetupResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "System setup value fetched successfully", systemSetupService.getById(id)));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponseDTO<SystemSetupResponseDTO>> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "System setup value fetched successfully", systemSetupService.getByCode(code)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<SystemSetupResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody SystemSetupUpdateRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "System setup value updated successfully", systemSetupService.update(id, requestDTO)));
    }
}
