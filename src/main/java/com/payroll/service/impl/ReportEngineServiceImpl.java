package com.payroll.service.impl;

import com.payroll.dto.response.ReportColumnDTO;
import com.payroll.dto.response.ReportDefinitionDTO;
import com.payroll.dto.response.ReportParameterDTO;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.service.ReportEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JDBC-backed implementation of the generic report engine (ADR-001, Option B).
 *
 * Mirrors the rest of this codebase's reporting style — stored procedures
 * named sp_rpt_*, returning List&lt;Map&lt;String,Object&gt;&gt; via JdbcTemplate —
 * but resolves *which* procedure to call and *how* to bind it from the
 * report_definition / report_parameter registry rather than from
 * hand-written controller/service methods.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportEngineServiceImpl implements ReportEngineService {

    private final JdbcTemplate jdbcTemplate;

    private static final String SELECT_DEFINITION_BASE =
            "SELECT id, report_key, label, description, icon, category, sp_name " +
            "FROM report_definition WHERE is_active = 'Y' ";

    @Override
    public List<ReportDefinitionDTO> listReports() {
        List<Map<String, Object>> defs = jdbcTemplate.queryForList(
                SELECT_DEFINITION_BASE + "ORDER BY display_order, label");
        return defs.stream().map(this::toDefinitionDto).collect(Collectors.toList());
    }

    @Override
    public ReportDefinitionDTO getReportDefinition(String reportKey) {
        Map<String, Object> def = findDefinitionRow(reportKey);
        return toDefinitionDto(def);
    }

    @Override
    @Transactional
    public List<Map<String, Object>> runReport(String reportKey, Map<String, String> filters, Long runBy) {
        Map<String, Object> def = findDefinitionRow(reportKey);
        Long definitionId = ((Number) def.get("id")).longValue();
        String spName = (String) def.get("sp_name");

        List<Map<String, Object>> paramRows = jdbcTemplate.queryForList(
                "SELECT param_key, label, is_required, default_value, sp_param_order " +
                "FROM report_parameter WHERE report_definition_id = ? ORDER BY sp_param_order",
                definitionId);

        Object[] boundValues = bindParameters(reportKey, paramRows, filters);

        String placeholders = paramRows.isEmpty()
                ? ""
                : String.join(", ", paramRows.stream().map(p -> "?").collect(Collectors.toList()));
        String callSql = String.format("CALL %s(%s)", spName, placeholders);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(callSql, boundValues);

        logRun(definitionId, runBy, filters, rows.size());

        return rows;
    }

    // ── Helpers ──────────────────────────────────────────────

    private Map<String, Object> findDefinitionRow(String reportKey) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                SELECT_DEFINITION_BASE + "AND report_key = ?", reportKey);
        if (rows.isEmpty()) {
            throw new ResourceNotFoundException("Report definition", "reportKey", reportKey);
        }
        return rows.get(0);
    }

    private ReportDefinitionDTO toDefinitionDto(Map<String, Object> def) {
        Long definitionId = ((Number) def.get("id")).longValue();
        return ReportDefinitionDTO.builder()
                .reportKey((String) def.get("report_key"))
                .label((String) def.get("label"))
                .description((String) def.get("description"))
                .icon((String) def.get("icon"))
                .category((String) def.get("category"))
                .parameters(loadParameters(definitionId))
                .columns(loadColumns(definitionId))
                .build();
    }

    private List<ReportParameterDTO> loadParameters(Long definitionId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT param_key, label, param_type, is_required, default_value " +
                "FROM report_parameter WHERE report_definition_id = ? ORDER BY sp_param_order",
                definitionId);
        return rows.stream().map(r -> ReportParameterDTO.builder()
                .paramKey((String) r.get("param_key"))
                .label((String) r.get("label"))
                .paramType((String) r.get("param_type"))
                .required("Y".equalsIgnoreCase(String.valueOf(r.get("is_required"))))
                .defaultValue((String) r.get("default_value"))
                .build()).collect(Collectors.toList());
    }

    private List<ReportColumnDTO> loadColumns(Long definitionId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT column_key, label, data_type, display_order " +
                "FROM report_column WHERE report_definition_id = ? AND is_visible = 'Y' " +
                "ORDER BY display_order",
                definitionId);
        return rows.stream().map(r -> ReportColumnDTO.builder()
                .columnKey((String) r.get("column_key"))
                .label((String) r.get("label"))
                .dataType((String) r.get("data_type"))
                .displayOrder(((Number) r.get("display_order")).intValue())
                .build()).collect(Collectors.toList());
    }

    /**
     * Resolves each configured parameter to a bound value, in sp_param_order:
     * use the supplied filter value, falling back to the configured default,
     * and rejecting missing required parameters with a clear message.
     */
    private Object[] bindParameters(String reportKey, List<Map<String, Object>> paramRows, Map<String, String> filters) {
        Object[] values = new Object[paramRows.size()];
        for (int i = 0; i < paramRows.size(); i++) {
            Map<String, Object> p = paramRows.get(i);
            String key = (String) p.get("param_key");
            boolean required = "Y".equalsIgnoreCase(String.valueOf(p.get("is_required")));
            String defaultValue = (String) p.get("default_value");

            String value = filters == null ? null : filters.get(key);
            if ((value == null || value.isBlank()) && defaultValue != null) {
                value = defaultValue;
            }
            if ((value == null || value.isBlank()) && required) {
                throw new IllegalArgumentException(
                        "Report '" + reportKey + "' requires parameter '" + key + "' (" + p.get("label") + ")");
            }
            values[i] = value;
        }
        return values;
    }

    private void logRun(Long definitionId, Long runBy, Map<String, String> filters, int rowCount) {
        jdbcTemplate.update(
                "INSERT INTO report_run_log (report_definition_id, run_by, parameters_json, row_count) " +
                "VALUES (?, ?, ?, ?)",
                definitionId, runBy, toJson(filters), rowCount);
    }

    /** Minimal, dependency-free JSON serialisation for the audit snapshot (flat string map only). */
    private String toJson(Map<String, String> filters) {
        if (filters == null || filters.isEmpty()) {
            return "{}";
        }
        Map<String, String> ordered = new LinkedHashMap<>(filters);
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : ordered.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(escape(entry.getKey())).append("\":\"")
              .append(escape(String.valueOf(entry.getValue()))).append('"');
        }
        return sb.append('}').toString();
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
