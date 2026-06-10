package com.payroll.license;

import java.time.LocalDate;

public record LicenseValidationResult(
        LicenseStatus status, String message, String licenseId, LicensePlan plan,
        int employeeLimit, long employeeCount, LocalDate validFrom, LocalDate validTill,
        boolean maintenanceAvailable) {
}
