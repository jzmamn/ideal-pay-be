package com.payroll.service;

import com.payroll.enums.ComponentType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Represents a single computed payroll component line produced by the
 * {@link SalaryCalculationEngineService}.  Each line maps to one
 * {@code EmpPayrollRunDetail} row after persistence.
 */
@Getter
@AllArgsConstructor
public class ComponentLine {

    private final ComponentType componentType;

    /** Null for system-generated lines (EPF_EE, EPF_ER, ETF, PAYE). */
    private final Long componentId;

    private final String componentCode;
    private final String componentName;
    private final BigDecimal amount;

    /** Populated for OT lines only; null otherwise. */
    private final BigDecimal hours;

    /** Populated for NOPAY lines only; null otherwise. */
    private final BigDecimal days;
}
