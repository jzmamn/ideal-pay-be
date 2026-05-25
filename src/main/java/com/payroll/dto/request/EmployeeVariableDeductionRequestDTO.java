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
public class EmployeeVariableDeductionRequestDTO {

    @NotNull(message = "Employee is required")
    private Long empId;

    @NotNull(message = "Variable deduction is required")
    private Long vdId;

    @NotNull(message = "Amount is required")
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
