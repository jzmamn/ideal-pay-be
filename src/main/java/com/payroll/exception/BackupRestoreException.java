package com.payroll.exception;

public class BackupRestoreException extends RuntimeException {

    public BackupRestoreException(String message) {
        super(message);
    }

    public BackupRestoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
