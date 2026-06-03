package com.payroll.enums;

public enum PayrollRunStatus {
    DRAFT,
    PROCESSED,
    LOCKED,
    /** Correction run created against a previously LOCKED run — still editable */
    CORRECTION_DRAFT,
    /** Correction run that has been locked/finalised */
    CORRECTION_LOCKED
}
