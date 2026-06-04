package com.payroll.controller;

import com.payroll.dto.request.PayslipEmailRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.PayrollRunSummaryDTO;
import com.payroll.dto.response.PayslipEmailResultDTO;
import com.payroll.service.PayrollRunService;
import com.payroll.service.PayslipEmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Payslip endpoints — read queries + email dispatch.
 * Base path: /payroll/payslip
 */
@RestController
@RequestMapping("/payroll/payslip")
@RequiredArgsConstructor
public class PayslipController {

    private final PayrollRunService  payrollRunService;
    private final PayslipEmailService payslipEmailService;

    // ── Read endpoints ────────────────────────────────────────────────────

    /** All run summaries for an employee (all months). */
    @GetMapping("/employee/{empId}")
    public ResponseEntity<ApiResponseDTO<List<PayrollRunSummaryDTO>>> getByEmployee(
            @PathVariable Long empId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Payslip runs fetched",
                payrollRunService.getPayrollRunsByEmployee(empId)));
    }

    /** All run summaries for a given payroll month e.g. "2026-05". */
    @GetMapping("/month/{payrollMonth}")
    public ResponseEntity<ApiResponseDTO<List<PayrollRunSummaryDTO>>> getByMonth(
            @PathVariable String payrollMonth) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Payslip runs fetched",
                payrollRunService.getPayrollRunsByMonth(payrollMonth)));
    }

    // ── Email endpoint ────────────────────────────────────────────────────

    /**
     * Email payslips to employees linked to the given run IDs.
     * POST /payroll/payslip/email
     */
    @PostMapping("/email")
    public ResponseEntity<ApiResponseDTO<PayslipEmailResultDTO>> emailPayslips(
            @Valid @RequestBody PayslipEmailRequestDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Payslip email dispatch complete",
                payslipEmailService.sendPayslips(request)));
    }
}
