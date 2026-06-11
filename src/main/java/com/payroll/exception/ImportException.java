package com.payroll.exception;

/** Recoverable import/export error — maps to HTTP 400. */
public class ImportException extends RuntimeException {

    public ImportException(String message) {
        super(message);
    }

    public ImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
