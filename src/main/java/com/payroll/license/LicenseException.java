package com.payroll.license;

import lombok.Getter;

@Getter
public class LicenseException extends RuntimeException {
    private final LicenseStatus status;

    public LicenseException(LicenseStatus status, String message) {
        super(message);
        this.status = status;
    }
}
