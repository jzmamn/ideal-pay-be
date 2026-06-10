package com.payroll.license;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LicenseImportRequest(
        @NotBlank(message = "licenseContent is required") String licenseContent,
        @NotNull(message = "installedBy is required") Long installedBy) {
}
