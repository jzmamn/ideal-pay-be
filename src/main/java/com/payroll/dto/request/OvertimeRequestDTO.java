package com.payroll.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OvertimeRequestDTO {


    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00", message = "Amount must be zero or greater")
    private BigDecimal amount;

    private Boolean isActive = true;

    /**
     * Optional MVEL formula expression for this overtime type.
     * When formulaEnabled is true, the formula result is used as the computed amount at payroll run time.
     * The fixed {@code amount} field acts as a fallback when formulaEnabled is false or formula is blank.
     */
    private String formula;

    /** When true, the formula is evaluated at payroll run time instead of using the fixed amount. */
    private Boolean formulaEnabled = false;

    @NotNull(message = "Created by is required")
    private Long createdBy;

    @NotNull(message = "Modified by is required")
    private Long modifiedBy;
}
