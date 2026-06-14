package com.payroll.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeOvertimeRequestDTO {

    @NotNull(message = "Employee is required")
    private Long empId;

    @NotNull(message = "Overtime type is required")
    private Long overtimeId;

    @NotNull(message = "Hours is required")
    @DecimalMin(value = "0.00", message = "Hours must be zero or greater")
    private BigDecimal hours;

    /**
     * Amount is derived server-side as rate × hours; clients must not send this.
     * Kept as an optional field (no @NotNull) for backward-compatibility with any
     * existing callers that may still supply it — the value is always ignored.
     */
    @DecimalMin(value = "0.00", message = "Amount must be zero or greater")
    private BigDecimal amount;

    @Size(max = 20, message = "Payroll month must not exceed 20 characters")
    private String payrollMonth;

    private Boolean isProcessed = false;

    private LocalDateTime processedDate;

    @NotNull(message = "Created by is required")
    private Long createdBy;

    @NotNull(message = "Modified by is required")
    private Long modifiedBy;
}
