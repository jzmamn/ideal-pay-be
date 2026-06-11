package com.payroll.service;

import com.payroll.dto.request.PayrollPeriodRequestDTO;
import com.payroll.dto.response.PayrollPeriodResponseDTO;
import com.payroll.enums.PayrollStatus;

import java.util.List;

/**
 * Payroll Period Management.
 * Lifecycle: FUTURE → OPEN → PROCESSING → COMPLETED → CLOSED ⇄ REOPENED.
 */
public interface PayrollPeriodService {

    // ── Queries ───────────────────────────────────────────────────────────────

    /** All periods, optionally filtered. Any filter may be null. */
    List<PayrollPeriodResponseDTO> getAllPeriods(Long companyId, Integer year,
                                                 Integer month, PayrollStatus status);

    PayrollPeriodResponseDTO getPeriod(Long id);

    /** The single active period for a company. */
    PayrollPeriodResponseDTO getActivePeriod(Long companyId);

    // ── CRUD ──────────────────────────────────────────────────────────────────

    PayrollPeriodResponseDTO createPeriod(PayrollPeriodRequestDTO request, Long userId);

    /** Update scheduling fields. Rejected when locked or CLOSED. */
    PayrollPeriodResponseDTO updatePeriod(Long id, PayrollPeriodRequestDTO request, Long userId);

    /** Delete only when no payroll transactions exist for the period. */
    void deletePeriod(Long id);

    // ── Status transitions ────────────────────────────────────────────────────

    /** Make this the company's active period; deactivates the previous one. */
    PayrollPeriodResponseDTO activatePeriod(Long id, Long userId);

    /** FUTURE/REOPENED → OPEN (data entry allowed). */
    PayrollPeriodResponseDTO openPeriod(Long id, Long userId);

    /** OPEN/REOPENED → PROCESSING; locks all payroll inputs. */
    PayrollPeriodResponseDTO startProcessing(Long id, Long userId);

    /** PROCESSING → COMPLETED; records payroll run date. */
    PayrollPeriodResponseDTO completePeriod(Long id, Long userId);

    /** COMPLETED (or OPEN/REOPENED) → CLOSED; locks the period. */
    PayrollPeriodResponseDTO closePeriod(Long id, Long userId);

    /** CLOSED → REOPENED; unlocks the period (authorized users only). */
    PayrollPeriodResponseDTO reopenPeriod(Long id, Long userId);

    // ── Legacy compatibility ──────────────────────────────────────────────────

    /**
     * True when data entry is allowed for the given YYYY-MM month
     * (no period exists yet, or the period status allows entry and is not locked).
     * Retained for callers that predate company-scoped periods
     * (e.g. EmployeeProfileServiceImpl).
     */
    boolean isPeriodOpen(String month);

    /** Company-scoped variant of {@link #isPeriodOpen(String)}. */
    boolean isPeriodOpen(Long companyId, String month);
}
