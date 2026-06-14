package com.payroll.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

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

    private Boolean isActive = true;

    /** Optional MVEL formula expression for this overtime type. */
    private String formula;

    /** When true, the formula is evaluated at payroll run time. */
    private Boolean formulaEnabled = false;

    /** Statutory liability settings — default to true (most OT types are fully liable). */
    private Boolean liableForEpf  = true;
    private Boolean liableForEtf  = true;
    private Boolean liableForPaye = true;
    /** When true, OT amount is proportionally reduced for no-pay days. Defaults to false. */
    private Boolean liableForNopay = false;

    @NotNull(message = "Created by is required")
    private Long createdBy;

    @NotNull(message = "Modified by is required")
    private Long modifiedBy;
}
