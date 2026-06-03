package com.payroll.controller;

import com.payroll.dto.request.SalaryIncrementRequest;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.SalaryIncrementResponse;
import com.payroll.enums.IncrementStatus;
import com.payroll.enums.IncrementType;
import com.payroll.service.SalaryIncrementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/salary-increment")
@RequiredArgsConstructor
public class SalaryIncrementController {

    private final SalaryIncrementService service;

    // ── Queries ───────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<SalaryIncrementResponse>>> getAll(
            @RequestParam(required = false) IncrementType   type,
            @RequestParam(required = false) IncrementStatus status,
            @RequestParam(required = false) String          effectiveMonth) {

        List<SalaryIncrementResponse> data;
        if (type   != null) data = service.getByType(type);
        else if (status != null) data = service.getByStatus(status);
        else if (effectiveMonth != null) data = service.getByEffectiveMonth(effectiveMonth);
        else data = service.getAll();

        return ResponseEntity.ok(ApiResponseDTO.success("Salary increments fetched", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<SalaryIncrementResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("Salary increment fetched", service.getById(id)));
    }

    @GetMapping("/employee/{empId}")
    public ResponseEntity<ApiResponseDTO<List<SalaryIncrementResponse>>> getByEmployee(@PathVariable Long empId) {
        return ResponseEntity.ok(ApiResponseDTO.success("Increments for employee fetched", service.getByEmployee(empId)));
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponseDTO<SalaryIncrementResponse>> create(
            @Valid @RequestBody SalaryIncrementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Salary increment created", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<SalaryIncrementResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SalaryIncrementRequest request) {
        return ResponseEntity.ok(ApiResponseDTO.success("Salary increment updated", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Salary increment deleted", null));
    }

    // ── Workflow actions ──────────────────────────────────────────────────────

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponseDTO<SalaryIncrementResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("Increment approved", service.approve(id)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponseDTO<SalaryIncrementResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("Increment cancelled", service.cancel(id)));
    }

    @PostMapping("/{id}/post")
    public ResponseEntity<ApiResponseDTO<SalaryIncrementResponse>> post(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("Increment posted", service.post(id)));
    }

    @GetMapping("/next-code")
    public ResponseEntity<ApiResponseDTO<String>> nextCode(@RequestParam String month) {
        return ResponseEntity.ok(ApiResponseDTO.success("Next code", service.nextCode(month)));
    }

    @PostMapping("/{id}/export")
    public ResponseEntity<ApiResponseDTO<SalaryIncrementResponse>> export(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("Increment exported to payroll", service.exportToPayroll(id)));
    }

    @PostMapping("/{id}/import")
    public ResponseEntity<ApiResponseDTO<SalaryIncrementResponse>> importFromPayroll(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("Current payroll values imported", service.importFromPayroll(id)));
    }
}
