package com.payroll.controller;

import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.PayrollRunResponseDTO;
import com.payroll.dto.response.PayrollRunSummaryDTO;
import com.payroll.service.PayrollRunService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/payroll-run")
@RequiredArgsConstructor
public class PayrollRunController {

    private final PayrollRunService payrollRunService;

    /** Process payroll for a single employee for the given month */
    @PostMapping("/process/{empId}/{payrollMonth}")
    public ResponseEntity<ApiResponseDTO<PayrollRunResponseDTO>> processPayroll(
            @PathVariable Long empId,
            @PathVariable String payrollMonth,
            @RequestParam Long processedBy) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(
                "Payroll processed successfully",
                payrollRunService.processPayroll(empId, payrollMonth, processedBy)));
    }

    /** Lock a payroll run — marks it immutable and stamps all component records as processed */
    @PostMapping("/lock/{runId}")
    public ResponseEntity<ApiResponseDTO<PayrollRunResponseDTO>> lockPayrollRun(
            @PathVariable Long runId,
            @RequestParam Long lockedBy) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Payroll run locked successfully",
                payrollRunService.lockPayrollRun(runId, lockedBy)));
    }

    /** Batch process payroll for all active employees for the given month */
    @PostMapping("/process-month/{payrollMonth}")
    public ResponseEntity<ApiResponseDTO<List<PayrollRunSummaryDTO>>> processPayrollForMonth(
            @PathVariable String payrollMonth,
            @RequestParam Long processedBy) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(
                "Batch payroll processed successfully",
                payrollRunService.processPayrollForMonth(payrollMonth, processedBy)));
    }

    /** Get full payroll run detail by run id */
    @GetMapping("/{runId}")
    public ResponseEntity<ApiResponseDTO<PayrollRunResponseDTO>> getPayrollRunById(
            @PathVariable Long runId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Payroll run fetched successfully",
                payrollRunService.getPayrollRunById(runId)));
    }

    /** Get all payroll runs for an employee (summary) */
    @GetMapping("/employee/{empId}")
    public ResponseEntity<ApiResponseDTO<List<PayrollRunSummaryDTO>>> getPayrollRunsByEmployee(
            @PathVariable Long empId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee payroll runs fetched successfully",
                payrollRunService.getPayrollRunsByEmployee(empId)));
    }

    /** Get all payroll runs for a payroll month (summary) */
    @GetMapping("/month/{payrollMonth}")
    public ResponseEntity<ApiResponseDTO<List<PayrollRunSummaryDTO>>> getPayrollRunsByMonth(
            @PathVariable String payrollMonth) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Payroll runs for month fetched successfully",
                payrollRunService.getPayrollRunsByMonth(payrollMonth)));
    }
}
