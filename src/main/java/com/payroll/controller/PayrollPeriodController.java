package com.payroll.controller;

import com.payroll.dto.request.PayrollPeriodRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.PayrollPeriodResponseDTO;
import com.payroll.enums.PayrollStatus;
import com.payroll.service.PayrollPeriodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Payroll Period Management.
 *
 * Permission checks ({@code @PreAuthorize}) take effect when
 * {@code app.security.method-security.enabled=true} — see MethodSecurityConfig.
 */
@RestController
@RequestMapping("/api/payroll-periods")
@RequiredArgsConstructor
public class PayrollPeriodController {

    private final PayrollPeriodService periodService;

    // ── Queries ───────────────────────────────────────────────────────────────

    /** All payroll periods; filter by company, year, month and/or status. */
    @GetMapping
    @PreAuthorize("hasAuthority('PAYROLL_PERIOD_VIEW')")
    public ResponseEntity<ApiResponseDTO<List<PayrollPeriodResponseDTO>>> getAll(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) PayrollStatus status) {
        return ResponseEntity.ok(ApiResponseDTO.success("Payroll periods fetched",
                periodService.getAllPeriods(companyId, year, month, status)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PAYROLL_PERIOD_VIEW')")
    public ResponseEntity<ApiResponseDTO<PayrollPeriodResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("Payroll period fetched",
                periodService.getPeriod(id)));
    }

    /** Active payroll period for a company. */
    @GetMapping("/active/{companyId}")
    @PreAuthorize("hasAuthority('PAYROLL_PERIOD_VIEW')")
    public ResponseEntity<ApiResponseDTO<PayrollPeriodResponseDTO>> getActive(
            @PathVariable Long companyId) {
        return ResponseEntity.ok(ApiResponseDTO.success("Active payroll period fetched",
                periodService.getActivePeriod(companyId)));
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAuthority('PAYROLL_PERIOD_CREATE')")
    public ResponseEntity<ApiResponseDTO<PayrollPeriodResponseDTO>> create(
            @Valid @RequestBody PayrollPeriodRequestDTO request,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(
                "Payroll period created", periodService.createPeriod(request, userId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PAYROLL_PERIOD_UPDATE')")
    public ResponseEntity<ApiResponseDTO<PayrollPeriodResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody PayrollPeriodRequestDTO request,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Payroll period updated", periodService.updatePeriod(id, request, userId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PAYROLL_PERIOD_DELETE')")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable Long id) {
        periodService.deletePeriod(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Payroll period deleted", null));
    }

    // ── Status transitions ────────────────────────────────────────────────────

    /** Make this the company's active period (deactivates the previous one). */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('PAYROLL_PERIOD_ACTIVATE')")
    public ResponseEntity<ApiResponseDTO<PayrollPeriodResponseDTO>> activate(
            @PathVariable Long id, @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Payroll period activated", periodService.activatePeriod(id, userId)));
    }

    /** FUTURE/REOPENED → OPEN. */
    @PatchMapping("/{id}/open")
    @PreAuthorize("hasAuthority('PAYROLL_PERIOD_UPDATE')")
    public ResponseEntity<ApiResponseDTO<PayrollPeriodResponseDTO>> open(
            @PathVariable Long id, @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Payroll period opened", periodService.openPeriod(id, userId)));
    }

    /** OPEN/REOPENED → PROCESSING; locks payroll inputs. */
    @PatchMapping("/{id}/start-processing")
    @PreAuthorize("hasAuthority('PAYROLL_PERIOD_PROCESS')")
    public ResponseEntity<ApiResponseDTO<PayrollPeriodResponseDTO>> startProcessing(
            @PathVariable Long id, @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Payroll period processing started", periodService.startProcessing(id, userId)));
    }

    /** PROCESSING → COMPLETED. */
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('PAYROLL_PERIOD_PROCESS')")
    public ResponseEntity<ApiResponseDTO<PayrollPeriodResponseDTO>> complete(
            @PathVariable Long id, @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Payroll period completed", periodService.completePeriod(id, userId)));
    }

    /** → CLOSED; locks the period. */
    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAuthority('PAYROLL_PERIOD_CLOSE')")
    public ResponseEntity<ApiResponseDTO<PayrollPeriodResponseDTO>> close(
            @PathVariable Long id, @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Payroll period closed", periodService.closePeriod(id, userId)));
    }

    /** CLOSED → REOPENED; unlocks the period. */
    @PatchMapping("/{id}/reopen")
    @PreAuthorize("hasAuthority('PAYROLL_PERIOD_REOPEN')")
    public ResponseEntity<ApiResponseDTO<PayrollPeriodResponseDTO>> reopen(
            @PathVariable Long id, @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Payroll period reopened", periodService.reopenPeriod(id, userId)));
    }
}
