package com.payroll.dto.request;

import com.payroll.enums.BonusCalculationMethod;
import jakarta.validation.constraints.*;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonusRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    /**
     * Optional — bonus calculation is formula-only, so there is only one valid
     * value ({@code FORMULA_BASED}). The server forces this value regardless
     * of what the client sends; clients no longer need to populate it.
     */
    private BonusCalculationMethod calculationMethod;

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

    /** MVEL formula expression (e.g. "basicSalary * 0.1" or a literal like "50000"). Required — bonus calculation is formula-only. */
    @NotBlank(message = "Formula is required")
    @Size(max = 500, message = "Formula must not exceed 500 characters")
    private String formula;

    @NotNull(message = "createdBy is required")
    private Long createdBy;

    @NotNull(message = "modifiedBy is required")
    private Long modifiedBy;
}
