package com.payroll.service;

import com.payroll.dto.response.PayrollPeriodResponseDTO;

import java.util.List;

public interface PayrollPeriodService {

    /** Open a new payroll period for the given month (YYYY-MM). Fails if already exists. */
    PayrollPeriodResponseDTO openPeriod(String month, Long userId);

    /**
     * Close the period for the given month.
     * Only allowed when every active employee has a LOCKED run for that month.
     */
    PayrollPeriodResponseDTO closePeriod(String month, Long userId);

    /** Force-close without the all-employees-locked check (admin override). */
    PayrollPeriodResponseDTO forceClosePeriod(String month, Long userId);

    /** Returns true if the period for the given month is OPEN (or does not yet exist). */
    boolean isPeriodOpen(String month);

    /** All periods, newest first. */
    List<PayrollPeriodResponseDTO> getAllPeriods();

    /** All OPEN periods. */
    List<PayrollPeriodResponseDTO> getOpenPeriods();

    /** Get one period by month. */
    PayrollPeriodResponseDTO getPeriod(String month);
}
