package com.payroll.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Request payload for the Employee &rarr; Salary Tab &rarr; Fixed Allowance checkbox grid.
 * Represents the complete desired set of Fixed Allowances for an employee for a single
 * payroll month: {@code selections} lists every allowance the user explicitly checked.
 * Any allowance previously assigned for this employee/month but absent from
 * {@code selections} is removed.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeFixedAllowanceAssignRequestDTO {

    @NotBlank(message = "Payroll month is required")
    @Size(max = 20, message = "Payroll month must not exceed 20 characters")
    private String payrollMonth;

    // createdBy / modifiedBy resolved server-side from the JWT security context

    @Valid
    @Builder.Default
    private List<Selection> selections = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Selection {

        @NotNull(message = "Fixed allowance is required")
        private Long faId;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.00", message = "Amount must be zero or greater")
        private BigDecimal amount;
    }
}
