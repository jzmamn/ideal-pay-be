package com.payroll.service;

import com.payroll.dto.response.PayrollRunReviewDTO;

import java.util.List;

public interface PayrollRunReviewService {

    /**
     * Returns a pre/post comparison for every employee × component
     * for the given payroll month.
     *
     * @param payrollMonth e.g. "2026-06"
     * @param viewedBy     user id — logged for audit
     */
    List<PayrollRunReviewDTO> getReview(String payrollMonth, Long viewedBy);
}
