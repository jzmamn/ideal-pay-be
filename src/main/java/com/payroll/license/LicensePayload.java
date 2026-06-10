package com.payroll.license;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record LicensePayload(
        String customerCode, String customerName, LicensePlan plan, int employeeLimit,
        LocalDate validFrom, LocalDate validTill, boolean maintenanceAvailable,
        OffsetDateTime issuedAt, String licenseId) {
}
