package com.payroll.service;

import com.payroll.dto.response.ReportDefinitionDTO;

import java.util.List;
import java.util.Map;

/**
 * Generic, config-driven report engine (ADR-001, Option B).
 * Reports are registered as metadata rows (report_definition /
 * report_parameter / report_column) rather than bespoke
 * controller+service+endpoint trios — adding a report becomes
 * "write a sp_rpt_ stored procedure and register it".
 */
public interface ReportEngineService {

    /** Active reports, in display order, for the report picker UI. */
    List<ReportDefinitionDTO> listReports();

    /** Full metadata (parameters + columns) for a single report. */
    ReportDefinitionDTO getReportDefinition(String reportKey);

    /**
     * Executes the report's backing stored procedure, binding the
     * supplied filter values in the order configured for the report,
     * and records an audit entry in report_run_log.
     *
     * @param reportKey stable id of the report, e.g. "payroll-summary"
     * @param filters   raw filter values keyed by paramKey (e.g. {"month": "2026-06"})
     * @param runBy     id of the user running the report (for audit logging)
     */
    List<Map<String, Object>> runReport(String reportKey, Map<String, String> filters, Long runBy);
}
