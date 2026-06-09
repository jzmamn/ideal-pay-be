package com.payroll.controller;

import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.ReportDefinitionDTO;
import com.payroll.service.ReportEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic report engine endpoints (ADR-001, Option B).
 *
 * Reports are driven entirely by the report_definition / report_parameter /
 * report_column registry — adding a new report means registering metadata
 * and writing a sp_rpt_ stored procedure, not adding new endpoints here.
 *
 * The legacy per-report endpoints under /payroll/pivot/*-report remain in
 * place as compatibility shims during the frontend migration (see
 * PayrollPivotController) and can be retired once all report UIs run
 * through this generic path.
 */
@RestController
@RequestMapping("/payroll/reports")
@RequiredArgsConstructor
public class ReportEngineController {

    private final ReportEngineService reportEngineService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<ReportDefinitionDTO>>> listReports() {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Report definitions fetched successfully",
                reportEngineService.listReports()));
    }

    @GetMapping("/{reportKey}")
    public ResponseEntity<ApiResponseDTO<ReportDefinitionDTO>> getReportDefinition(
            @PathVariable String reportKey) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Report definition fetched successfully",
                reportEngineService.getReportDefinition(reportKey)));
    }

    @GetMapping("/{reportKey}/run")
    public ResponseEntity<ApiResponseDTO<List<Map<String, Object>>>> runReport(
            @PathVariable String reportKey,
            @RequestParam(required = false) Long runBy,
            @RequestParam Map<String, String> allParams) {

        // Strip the audit param out of the filter map before binding to the SP.
        Map<String, String> filters = new LinkedHashMap<>(allParams);
        filters.remove("runBy");

        return ResponseEntity.ok(ApiResponseDTO.success(
                "Report executed successfully",
                reportEngineService.runReport(reportKey, filters, runBy != null ? runBy : 1L)));
    }
}
