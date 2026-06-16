package com.payroll.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedAllowanceRequestDTO {


    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

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

    /**
     * Static fixed amount applied to all employees when no formula is configured.
     * Mutually exclusive with {@code formula}.
     * Null means no company-level default (per-employee manual entry via batch screen).
     */
    @DecimalMin(value = "0.00", message = "Amount must be zero or greater")
    private BigDecimal amount;

    /** Optional MVEL formula expression (e.g. "basicSalary * 0.1"). When present, it is evaluated at load time instead of using the static amount. */
    @Size(max = 500, message = "Formula must not exceed 500 characters")
    private String formula;

    @NotNull(message = "createdBy is required")
    private Long createdBy;

    @NotNull(message = "modifiedBy is required")
    private Long modifiedBy;
}
