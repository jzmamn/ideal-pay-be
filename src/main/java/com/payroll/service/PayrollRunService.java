package com.payroll.service;

import com.payroll.dto.response.PayrollRunResponseDTO;
import com.payroll.dto.response.PayrollRunSummaryDTO;

import java.util.List;

public interface PayrollRunService {

    /** Process payroll for a single employee for the given month. Creates a DRAFT run. */
    PayrollRunResponseDTO processPayroll(Long empId, String payrollMonth, Long processedBy);

    /** Lock a run — moves status from PROCESSED → LOCKED and marks component records as processed. */
    PayrollRunResponseDTO lockPayrollRun(Long runId, Long lockedBy);

    /** Process payroll for all active employees for the given month (batch). */
    List<PayrollRunSummaryDTO> processPayrollForMonth(String payrollMonth, Long processedBy);

    /** Get full run detail by id. */
    PayrollRunResponseDTO getPayrollRunById(Long runId);

    /** Get all runs for an employee (summary list). */
    List<PayrollRunSummaryDTO> getPayrollRunsByEmployee(Long empId);

    /** Get all runs for a payroll month (summary list). */
    List<PayrollRunSummaryDTO> getPayrollRunsByMonth(String payrollMonth);
}
