package com.payroll.service;

import com.payroll.dto.request.BonusBatchActionRequestDTO;
import com.payroll.dto.request.BonusEntryAdjustRequestDTO;
import com.payroll.dto.request.BonusProcessingCalculateRequestDTO;
import com.payroll.dto.response.*;

import java.util.List;

public interface BonusProcessingService {

    /** Calculate bonuses and create a new processing batch. */
    BonusProcessingBatchResponseDTO calculate(BonusProcessingCalculateRequestDTO request);

    /** List all batches (summary — no entries). */
    List<BonusProcessingBatchResponseDTO> getAllBatches(String payrollMonth, String status);

    /** Get a single batch with its employee entries. */
    BonusProcessingBatchResponseDTO getBatchById(Long batchId);

    /** Adjust a single employee entry amount within a batch. */
    EmployeeBonusProcessingRowDTO adjustEntry(Long batchId, Long entryId, BonusEntryAdjustRequestDTO request);

    /** Approve the entire batch (transitions PENDING → APPROVED). */
    BonusProcessingBatchResponseDTO approveBatch(Long batchId, BonusBatchActionRequestDTO request);

    /** Process (pay) the approved batch (transitions APPROVED → PROCESSED). */
    BonusProcessingBatchResponseDTO processBatch(Long batchId, BonusBatchActionRequestDTO request);

    /** Cancel the batch (any non-PROCESSED status → CANCELLED). */
    BonusProcessingBatchResponseDTO cancelBatch(Long batchId, BonusBatchActionRequestDTO request);

    // ── Reports ──────────────────────────────────────────────────────────────

    List<BonusSummaryReportDTO> getSummaryReport(String payrollMonth);

    List<EmployeeBonusProcessingRowDTO> getEmployeeReport(String payrollMonth, Long bonusId);

    List<BonusDepartmentReportDTO> getDepartmentReport(String payrollMonth);

    /**
     * Approval report — all batches grouped by status (PENDING / APPROVED / PROCESSED / CANCELLED).
     * Optionally filtered by payrollMonth.
     */
    List<BonusApprovalReportDTO> getApprovalReport(String payrollMonth);
}
