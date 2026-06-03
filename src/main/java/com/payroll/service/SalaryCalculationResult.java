package com.payroll.service;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Immutable result produced by {@link SalaryCalculationEngineService}.
 * Passed back to {@code PayrollRunService} for persistence.
 */
@Getter
@Builder
public class SalaryCalculationResult {

    private final BigDecimal basicSalary;
    private final BigDecimal totalAllowances;
    private final BigDecimal totalDeductions;
    private final BigDecimal grossPay;
    private final BigDecimal netPay;

    // Statutory
    private final BigDecimal epfLiableBase;
    private final BigDecimal employeeEpf;
    private final BigDecimal employerEpf;
    private final BigDecimal etf;
    private final BigDecimal payeTax;

    private final int workingDays;

    /** All component lines (FA, VA, OT, NOPAY, FD, VD, EPF_EE, EPF_ER, ETF, PAYE). */
    private final List<ComponentLine> lines;
}
