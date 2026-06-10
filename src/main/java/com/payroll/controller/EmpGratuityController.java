package com.payroll.controller;

import com.payroll.dto.request.EmpGratuityRequest;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.EmpGratuityResponse;
import com.payroll.enums.GratuityStatus;
import com.payroll.service.EmpGratuityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/gratuity")
@RequiredArgsConstructor
public class EmpGratuityController {

    private final EmpGratuityService service;

    // ── Queries ────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<EmpGratuityResponse>>> getAll(
            @RequestParam(required = false) GratuityStatus status,
            @RequestParam(required = false) Long empId) {

        List<EmpGratuityResponse> data;
        if (status != null)   data = service.getByStatus(status);
        else if (empId != null) data = service.getByEmployee(empId);
        else                    data = service.getAll();

        return ResponseEntity.ok(ApiResponseDTO.success("Gratuities fetched", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmpGratuityResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("EmpGratuity fetched", service.getById(id)));
    }

    @GetMapping("/next-code")
    public ResponseEntity<ApiResponseDTO<String>> nextCode() {
        return ResponseEntity.ok(ApiResponseDTO.success("Next code", service.nextCode()));
    }

    // ── Mutations ──────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponseDTO<EmpGratuityResponse>> create(
            @Valid @RequestBody EmpGratuityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("EmpGratuity created", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmpGratuityResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody EmpGratuityRequest request) {
        return ResponseEntity.ok(ApiResponseDTO.success("EmpGratuity updated", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponseDTO.success("EmpGratuity deleted", null));
    }

    // ── Workflow ───────────────────────────────────────────────────────────────

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponseDTO<EmpGratuityResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("EmpGratuity approved", service.approve(id)));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponseDTO<EmpGratuityResponse>> markPaid(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("EmpGratuity marked as paid", service.markPaid(id)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponseDTO<EmpGratuityResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("EmpGratuity cancelled", service.cancel(id)));
    }
}
