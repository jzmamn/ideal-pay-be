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
public class GradeRequestDTO {
    // code is auto-generated as GRD_<id> on create — not accepted from caller

    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String name;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00", message = "Amount must be zero or greater")
    private BigDecimal amount;

    @Builder.Default
    private Boolean isActive = true;

    @NotNull(message = "Created by is required")
    private Long createdBy;

    @NotNull(message = "Modified by is required")
    private Long modifiedBy;
}
