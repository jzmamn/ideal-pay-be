package com.payroll.enums;

/** Lifecycle of an import: committed, undone, or frozen by payroll processing. */
public enum ImportStatus {
    COMMITTED,
    ROLLED_BACK,
    LOCKED
}
