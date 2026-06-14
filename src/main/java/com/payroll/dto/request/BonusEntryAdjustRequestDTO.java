package com.payroll.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

/** Adjusts the bonus amount for a single employee entry within a batch. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BonusEntryAdjustRequestDTO {

    @NotNull(message = "Adjusted amount is required")
    @DecimalMin(value = "0.00", message = "Adjusted amount must be >= 0")
    private BigDecimal adjustedAmount;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;

    @NotNull(message = "Modified by is required")
    private Long modifiedBy;
}
