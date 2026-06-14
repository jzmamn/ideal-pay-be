package com.payroll.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LateDeductionConfigRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull(message = "Working days is required")
    @Min(value = 1, message = "Working days must be at least 1")
    @Max(value = 31, message = "Working days must not exceed 31")
    private Integer workingDays;

    @NotNull(message = "Working hours per day is required")
    @Min(value = 1, message = "Working hours per day must be at least 1")
    @Max(value = 24, message = "Working hours per day must not exceed 24")
    private Integer workingHoursPerDay;

    private String formula;

    private Boolean formulaEnabled = false;

    private Boolean isActive = true;

    private Boolean liableForEpf   = true;
    private Boolean liableForEtf   = true;
    private Boolean liableForPaye  = true;
    private Boolean liableForNopay = false;

    @NotNull(message = "Created by is required")
    private Long createdBy;

    @NotNull(message = "Modified by is required")
    private Long modifiedBy;
}
