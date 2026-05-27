package com.payroll.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedDeductionRequestDTO {


    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Amount must be 0 or greater")
    @Digits(integer = 16, fraction = 2, message = "Amount format is invalid")
    private BigDecimal amount;

    @NotNull(message = "isActive is required")
    private Boolean isActive;

    @NotNull(message = "liableForEpf is required")
    private Boolean liableForEpf;

    @NotNull(message = "liableForEtf is required")
    private Boolean liableForEtf;

    @NotNull(message = "liableForPaye is required")
    private Boolean liableForPaye;

    @NotNull(message = "liableNoPay is required")
    private Boolean liableNoPay;

    /** Optional MVEL formula expression (e.g. "basicSalary * 0.05"). */
    @Size(max = 500, message = "Formula must not exceed 500 characters")
    private String formula;

    /** When true, the formula is used at payroll run time instead of the fixed amount. Defaults to false. */
    private Boolean formulaEnabled = false;

    @NotNull(message = "createdBy is required")
    private Long createdBy;

    @NotNull(message = "modifiedBy is required")
    private Long modifiedBy;
}
