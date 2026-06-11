package com.payroll.enums;

/**
 * Lifecycle stages of a payroll period.
 *
 * <pre>
 * FUTURE → OPEN → PROCESSING → COMPLETED → CLOSED ⇄ REOPENED
 * </pre>
 *
 * Replaces the legacy {@code PeriodStatus} (OPEN | CLOSED).
 */
public enum PayrollStatus {

    /** Created ahead of time; no data entry yet. */
    FUTURE,

    /** Data entry allowed (salary, attendance, OT, allowances, deductions). */
    OPEN,

    /** Payroll run in progress — all payroll inputs are locked. */
    PROCESSING,

    /** Payroll run finished — payslips, bank files and reports available. */
    COMPLETED,

    /** Read-only. Can only be edited after an authorized reopen. */
    CLOSED,

    /** A previously CLOSED period reopened by an authorized user. */
    REOPENED;

    /** Statuses in which payroll data entry is permitted. */
    public boolean allowsDataEntry() {
        return this == OPEN || this == REOPENED;
    }

    /** Read-only statuses. */
    public boolean isReadOnly() {
        return this == CLOSED;
    }
}
