package com.payroll.service;

import com.payroll.dto.request.CorrectionDetailUpdateDTO;
import com.payroll.dto.response.PayrollRunResponseDTO;
import com.payroll.dto.response.PayrollRunSummaryDTO;

import java.util.List;

public interface PayrollRunService {

    /** Process payroll for a single employee for the given month. Creates a DRAFT run. */
    PayrollRunResponseDTO processPayroll(Long empId, String payrollMonth, Long processedBy);

    /** Lock a run — moves status DRAFT/PROCESSED → LOCKED and stamps component records. */
    PayrollRunResponseDTO lockPayrollRun(Long runId, Long lockedBy);

    /** Batch process payroll for all active employees for the given month. */
    List<PayrollRunSummaryDTO> processPayrollForMonth(String payrollMonth, Long processedBy);

    // ── Correction ────────────────────────────────────────────────────────────

    /**
     * Create a CORRECTION_DRAFT run against a LOCKED run.
     * The correction run is seeded with the original run's detail lines so the
     * user can see current figures and amend only what changed.
     */
    PayrollRunResponseDTO createCorrectionRun(Long originalRunId, Long userId);

    /**
     * Replace the detail lines of a CORRECTION_DRAFT run.
     * Recalculates totals automatically.
     */
    PayrollRunResponseDTO updateCorrectionDetails(Long correctionRunId, List<CorrectionDetailUpdateDTO> details, Long userId);

    /**
     * Lock a CORRECTION_DRAFT run → CORRECTION_LOCKED.
     * No component-record stamps needed (correction runs are self-contained).
     */
    PayrollRunResponseDTO lockCorrectionRun(Long correctionRunId, Long userId);

    // ── Queries ───────────────────────────────────────────────────────────────

    /** Get full run detail by id. */
    PayrollRunResponseDTO getPayrollRunById(Long runId);

    /** All runs for an employee — includes both NORMAL and CORRECTION runs. */
    List<PayrollRunSummaryDTO> getPayrollRunsByEmployee(Long empId);

    /** All runs for a payroll month — includes both NORMAL and CORRECTION runs. */
    List<PayrollRunSummaryDTO> getPayrollRunsByMonth(String payrollMonth);

    /** All CORRECTION runs for a given original run id. */
    List<PayrollRunSummaryDTO> getCorrectionsByOriginalRun(Long originalRunId);
}
