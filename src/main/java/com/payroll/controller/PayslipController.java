package com.payroll.controller;

import com.payroll.dto.request.PayslipEmailRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.PayrollRunSummaryDTO;
import com.payroll.dto.response.PayslipEmailResultDTO;
import com.payroll.entity.Company;
import com.payroll.entity.EmpPayrollRun;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.repository.CompanyRepository;
import com.payroll.repository.EmpPayrollRunRepository;
import com.payroll.service.PayrollRunService;
import com.payroll.service.PayslipEmailService;
import com.payroll.service.PayslipPdfService;
import com.payroll.service.impl.PayslipTokenMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.Map;

/**
 * Payslip endpoints — run queries, PDF generation, and email dispatch.
 * Base path: /payroll/payslip
 *
 * PDF endpoints:
 *   GET  /pdf/{runId}            → single payslip
 *   POST /pdf/selected           → merged PDF for a list of run IDs
 *   GET  /pdf/batch/{month}      → merged PDF for all runs in a payroll month
 */
@RestController
@RequestMapping("/payroll/payslip")
@RequiredArgsConstructor
public class PayslipController {

    private final PayrollRunService      payrollRunService;
    private final PayslipEmailService    payslipEmailService;
    private final PayslipPdfService      payslipPdfService;
    private final PayslipTokenMapper     payslipTokenMapper;
    private final EmpPayrollRunRepository runRepo;
    private final CompanyRepository      companyRepo;

    // ── Run query endpoints ───────────────────────────────────────────────

    /** All run summaries for an employee (all months). */
    @GetMapping("/employee/{empId}")
    public ResponseEntity<ApiResponseDTO<List<PayrollRunSummaryDTO>>> getByEmployee(
            @PathVariable Long empId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Payslip runs fetched",
                payrollRunService.getPayrollRunsByEmployee(empId)));
    }

    /** All run summaries for a payroll month e.g. "2026-05". */
    @GetMapping("/month/{payrollMonth}")
    public ResponseEntity<ApiResponseDTO<List<PayrollRunSummaryDTO>>> getByMonth(
            @PathVariable String payrollMonth) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Payslip runs fetched",
                payrollRunService.getPayrollRunsByMonth(payrollMonth)));
    }

    // ── PDF endpoints ─────────────────────────────────────────────────────

    /**
     * Single payslip PDF for one payroll run.
     * GET /payroll/payslip/pdf/{runId}?templateId=1
     */
    @GetMapping("/pdf/{runId}")
    public ResponseEntity<StreamingResponseBody> downloadSingle(
            @PathVariable Long runId,
            @RequestParam(required = false) Long templateId) {
        return pdfResponse(
                payslipPdfService.generatePdf(runId, templateId),
                "payslip-" + runId + ".pdf");
    }

    /**
     * Merged PDF for a selected list of run IDs (one payslip per page).
     * POST /payroll/payslip/pdf/selected
     * Body: [1, 2, 3]
     */
    @PostMapping("/pdf/selected")
    public ResponseEntity<StreamingResponseBody> downloadSelected(
            @RequestBody @NotEmpty(message = "At least one run ID is required") List<Long> runIds,
            @RequestParam(required = false) Long templateId) {
        return pdfResponse(
                payslipPdfService.generatePdfForSelected(runIds, templateId),
                "payslips-selected.pdf");
    }

    /**
     * Merged PDF for all employees in a payroll month.
     * GET /payroll/payslip/pdf/batch/{payrollMonth}
     * Example: /payroll/payslip/pdf/batch/2026-05
     */
    @GetMapping("/pdf/batch/{payrollMonth}")
    public ResponseEntity<StreamingResponseBody> downloadBatch(
            @PathVariable String payrollMonth) {
        return pdfResponse(
                payslipPdfService.generatePdfForMonth(payrollMonth),
                "payslips-" + payrollMonth + ".pdf");
    }

    /**
     * A4-landscape PDF with two payslips side by side in portrait orientation.
     * POST /payroll/payslip/pdf/2up
     * Body: [runId1, runId2, runId3, ...]   — pairs grouped left→right per page.
     * Odd number of IDs → last page has one payslip on the left, blank right.
     */
    @PostMapping("/pdf/2up")
    public ResponseEntity<StreamingResponseBody> download2Up(
            @RequestBody @NotEmpty(message = "At least one run ID is required") List<Long> runIds,
            @RequestParam(required = false) Long templateId) {
        return pdfResponse(
                payslipPdfService.generatePdf2Up(runIds, templateId),
                "payslips-2up.pdf");
    }

    // ── Token debug endpoint ──────────────────────────────────────────────

    /**
     * Returns the full resolved token map for a payroll run as JSON.
     * Useful for verifying that all {{TOKEN}} placeholders will be populated.
     *
     * GET /payroll/payslip/tokens/{runId}
     *
     * Example response:
     * {
     *   "BASIC_SALARY": "50,000.00",
     *   "NOPAY": "1,923.08",
     *   "FA_HRA": "5,000.00",
     *   "lbl_FA_HRA": "House Rent Allowance",
     *   ...
     * }
     */
    @GetMapping("/tokens/{runId}")
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> debugTokens(
            @PathVariable Long runId) {
        EmpPayrollRun run = runRepo.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", "id", runId));
        Company company = companyRepo.findAllByIsActive(true, Sort.by("id"))
                .stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No active company found."));
        Map<String, String> tokens = payslipTokenMapper.buildTokens(run, company);
        return ResponseEntity.ok(ApiResponseDTO.success("Token map for run " + runId, tokens));
    }

    // ── Email endpoint ────────────────────────────────────────────────────

    @PostMapping("/email")
    public ResponseEntity<ApiResponseDTO<PayslipEmailResultDTO>> emailPayslips(
            @Valid @RequestBody PayslipEmailRequestDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Payslip email dispatch complete",
                payslipEmailService.sendPayslips(request)));
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private static ResponseEntity<StreamingResponseBody> pdfResponse(
            StreamingResponseBody body, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(body);
    }
}
