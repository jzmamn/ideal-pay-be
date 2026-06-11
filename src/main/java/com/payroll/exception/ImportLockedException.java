package com.payroll.exception;

/**
 * Rollback refused because at least one imported row has already been
 * processed by a payroll run — maps to HTTP 409 Conflict.
 */
public class ImportLockedException extends RuntimeException {

    public ImportLockedException(String message) {
        super(message);
    }
}
