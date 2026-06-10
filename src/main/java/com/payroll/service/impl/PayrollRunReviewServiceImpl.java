package com.payroll.service.impl;

import com.payroll.dto.response.PayrollRunReviewDTO;
import com.payroll.service.PayrollRunReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PayrollRunReviewServiceImpl implements PayrollRunReviewService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<PayrollRunReviewDTO> getReview(String payrollMonth, Long viewedBy) {

        // ── 1. Fetch review rows via SP ───────────────────────────────────────
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "CALL sp_get_payroll_run_review(?)", payrollMonth);

        // ── 2. Write audit log (fire-and-forget; don't fail the read if it errors) ──
        try {
            jdbcTemplate.update(
                    "INSERT INTO payroll_run_review_log (payroll_month, viewed_by) VALUES (?, ?)",
                    payrollMonth, viewedBy);
        } catch (Exception ex) {
            log.warn("Failed to write payroll_run_review_log for month={}: {}", payrollMonth, ex.getMessage());
        }

        // ── 3. Map raw rows → DTO ─────────────────────────────────────────────
        return rows.stream()
                .map(this::mapRow)
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private PayrollRunReviewDTO mapRow(Map<String, Object> row) {
        return PayrollRunReviewDTO.builder()
                .empId(str(row, "emp_id"))
                .empName(str(row, "emp_name"))
                .componentType(str(row, "component_type"))
                .componentCode(str(row, "component_code"))
                .componentName(str(row, "component_name"))
                .hasFormula(str(row, "has_formula"))
                .formulaExpr(str(row, "formula_expr"))
                .beforeValue(decimal(row, "before_value"))
                .afterValue(decimal(row, "after_value"))
                .difference(decimal(row, "difference"))
                .runStatus(str(row, "run_status"))
                .build();
    }

    private static String str(Map<String, Object> row, String key) {
        Object val = row.get(key);
        return val == null ? null : val.toString();
    }

    private static BigDecimal decimal(Map<String, Object> row, String key) {
        Object val = row.get(key);
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal bd) return bd;
        return new BigDecimal(val.toString());
    }
}
