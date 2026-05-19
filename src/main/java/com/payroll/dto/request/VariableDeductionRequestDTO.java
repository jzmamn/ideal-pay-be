package com.payroll.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariableDeductionRequestDTO {

    @NotBlank(message = "Code is required")
    @Size(max = 10, message = "Code must not exceed 10 characters")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

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
