package com.payroll.config;

/**
 * Role-based permission authorities for Payroll Period Management.
 *
 * Enforcement is via {@code @PreAuthorize} on PayrollPeriodController and is
 * activated by setting {@code app.security.method-security.enabled=true}
 * (see {@link MethodSecurityConfig}). It is off by default so the current
 * permit-all setup keeps working until authentication is wired up.
 */
public final class PayrollPeriodPermissions {

    public static final String VIEW     = "PAYROLL_PERIOD_VIEW";
    public static final String CREATE   = "PAYROLL_PERIOD_CREATE";
    public static final String UPDATE   = "PAYROLL_PERIOD_UPDATE";
    public static final String ACTIVATE = "PAYROLL_PERIOD_ACTIVATE";
    public static final String PROCESS  = "PAYROLL_PERIOD_PROCESS";
    public static final String CLOSE    = "PAYROLL_PERIOD_CLOSE";
    public static final String REOPEN   = "PAYROLL_PERIOD_REOPEN";
    public static final String DELETE   = "PAYROLL_PERIOD_DELETE";

    private PayrollPeriodPermissions() {
    }
}
