package com.payroll.controller;

import com.payroll.dto.request.BonusBatchActionRequestDTO;
import com.payroll.dto.request.BonusEntryAdjustRequestDTO;
import com.payroll.dto.request.BonusProcessingCalculateRequestDTO;
import com.payroll.dto.response.*;
import com.payroll.service.BonusProcessingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/bonus-processing")
@RequiredArgsConstructor
public class BonusProcessingController {

    private final BonusProcessingService service;

    // ── Batch lifecycle ───────────────────────────────────────────────────────

    @PostMapping("/calculate")
    public ResponseEntity<ApiResponseDTO<BonusProcessingBatchResponseDTO>> calculate(
            @Valid @RequestBody BonusProcessingCalculateRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Bonus calculation completed and batch created",
                        service.calculate(request)));
    }

    @GetMapping("/batches")
    public ResponseEntity<ApiResponseDTO<List<BonusProcessingBatchResponseDTO>>> getAllBatches(
            @RequestParam(required = false) String payrollMonth,
            @RequestParam(required = false, defaultValue = "all") String status) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Bonus processing batches fetched successfully",
                service.getAllBatches(payrollMonth, status)));
    }

    @GetMapping("/batches/{id}")
    public ResponseEntity<ApiResponseDTO<BonusProcessingBatchResponseDTO>> getBatchById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Bonus processing batch fetched successfully",
                service.getBatchById(id)));
    }

    @PostMapping("/batches/{id}/approve")
    public ResponseEntity<ApiResponseDTO<BonusProcessingBatchResponseDTO>> approveBatch(
            @PathVariable Long id,
            @Valid @RequestBody BonusBatchActionRequestDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Bonus batch approved successfully",
                service.approveBatch(id, request)));
    }

    @PostMapping("/batches/{id}/process")
    public ResponseEntity<ApiResponseDTO<BonusProcessingBatchResponseDTO>> processBatch(
            @PathVariable Long id,
            @Valid @RequestBody BonusBatchActionRequestDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Bonus batch processed successfully",
                service.processBatch(id, request)));
    }

    @PostMapping("/batches/{id}/cancel")
    public ResponseEntity<ApiResponseDTO<BonusProcessingBatchResponseDTO>> cancelBatch(
            @PathVariable Long id,
            @Valid @RequestBody BonusBatchActionRequestDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Bonus batch cancelled",
                service.cancelBatch(id, request)));
    }

    // ── Entry-level adjustment ────────────────────────────────────────────────

    @PutMapping("/batches/{batchId}/entries/{entryId}")
    public ResponseEntity<ApiResponseDTO<EmployeeBonusProcessingRowDTO>> adjustEntry(
            @PathVariable Long batchId,
            @PathVariable Long entryId,
            @Valid @RequestBody BonusEntryAdjustRequestDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Entry adjusted successfully",
                service.adjustEntry(batchId, entryId, request)));
    }

    // ── Reports ───────────────────────────────────────────────────────────────

    @GetMapping("/reports/summary")
    public ResponseEntity<ApiResponseDTO<List<BonusSummaryReportDTO>>> summaryReport(
            @RequestParam(required = false) String payrollMonth) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Bonus summary report fetched",
                service.getSummaryReport(payrollMonth)));
    }

    @GetMapping("/reports/employee")
    public ResponseEntity<ApiResponseDTO<List<EmployeeBonusProcessingRowDTO>>> employeeReport(
            @RequestParam(required = false) String payrollMonth,
            @RequestParam(required = false) Long bonusId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee bonus report fetched",
                service.getEmployeeReport(payrollMonth, bonusId)));
    }

    @GetMapping("/reports/department")
    public ResponseEntity<ApiResponseDTO<List<BonusDepartmentReportDTO>>> departmentReport(
            @RequestParam(required = false) String payrollMonth) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Department bonus report fetched",
                service.getDepartmentReport(payrollMonth)));
    }

    @GetMapping("/reports/approval")
    public ResponseEntity<ApiResponseDTO<List<BonusApprovalReportDTO>>> approvalReport(
            @RequestParam(required = false) String payrollMonth) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Bonus approval report fetched",
                service.getApprovalReport(payrollMonth)));
    }
}
