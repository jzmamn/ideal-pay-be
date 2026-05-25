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

    @NotNull(message = "createdBy is required")
    private Long createdBy;

    @NotNull(message = "modifiedBy is required")
    private Long modifiedBy;
}
