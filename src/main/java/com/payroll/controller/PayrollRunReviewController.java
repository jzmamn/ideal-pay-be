package com.payroll.controller;

import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.PayrollRunReviewDTO;
import com.payroll.service.PayrollRunReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * GET /payroll/run-review?month=2026-06&viewedBy=1
 *
 * Returns a pre/post comparison table for every employee × payroll component
 * for the given month. Reads are always live from emp_payroll_run_detail;
 * the endpoint is intentionally read-only (no POST/PUT/DELETE).
 *
 * The response is immutable once the payroll month is LOCKED — no separate
 * lock check is needed here because the underlying run detail records are
 * never modified after locking.
 */
@RestController
@RequestMapping("/payroll/run-review")
@RequiredArgsConstructor
public class PayrollRunReviewController {

    private final PayrollRunReviewService reviewService;

    /**
     * Fetch the payroll run review for a given month.
     *
     * @param month     payroll month string, e.g. "2026-06"
     * @param viewedBy  user id of the person viewing (for audit log)
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<PayrollRunReviewDTO>>> getReview(
            @RequestParam String month,
            @RequestParam(defaultValue = "1") Long viewedBy) {

        List<PayrollRunReviewDTO> data = reviewService.getReview(month, viewedBy);
        return ResponseEntity.ok(
                ApiResponseDTO.success("Payroll run review fetched successfully", data));
    }
}
