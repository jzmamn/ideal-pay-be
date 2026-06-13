package com.payroll.service;

import java.math.BigDecimal;

/**
 * Immutable snapshot of statutory payroll rates and feature flags loaded from
 * the {@code system_setup} table at the start of each payroll calculation.
 *
 * <p>Codes consumed:
 * <ul>
 *   <li>{@code EMPLOYEE_EPF_PERCENT}  — employee EPF contribution (e.g. 8.00)</li>
 *   <li>{@code EMPLOYER_EPF_PERCENT}  — employer EPF contribution (e.g. 12.00)</li>
 *   <li>{@code EMPLOYER_ETF_PERCENT}  — employer ETF contribution (e.g. 3.00)</li>
 *   <li>{@code EPF_ENABLED}           — 'Y' / 'N' feature flag</li>
 *   <li>{@code ETF_ENABLED}           — 'Y' / 'N' feature flag</li>
 *   <li>{@code PAYE_ENABLED}          — 'Y' / 'N' feature flag</li>
 * </ul>
 *
 * @param epfEeRate    employee EPF rate as a decimal fraction (e.g. 0.08 for 8%)
 * @param epfErRate    employer EPF rate as a decimal fraction (e.g. 0.12 for 12%)
 * @param etfRate      employer ETF rate as a decimal fraction (e.g. 0.03 for 3%)
 * @param epfEnabled   whether EPF calculations should run
 * @param etfEnabled   whether ETF calculations should run
 * @param payeEnabled  whether PAYE tax calculations should run
 */
public record PayrollRates(
        BigDecimal epfEeRate,
        BigDecimal epfErRate,
        BigDecimal etfRate,
        boolean    epfEnabled,
        boolean    etfEnabled,
        boolean    payeEnabled
) {
    /** Convenience: percentage string for the employee EPF rate (e.g. "8"). */
    public String epfEePct() {
        return epfEeRate.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString();
    }

    /** Convenience: percentage string for the employer EPF rate (e.g. "12"). */
    public String epfErPct() {
        return epfErRate.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString();
    }

    /** Convenience: percentage string for the ETF rate (e.g. "3"). */
    public String etfPct() {
        return etfRate.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString();
    }
}
