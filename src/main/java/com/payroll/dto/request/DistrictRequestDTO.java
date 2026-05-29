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
public class DistrictRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 70, message = "Name must not exceed 70 characters")
    private String name;

    @Builder.Default
    private Boolean isActive = true;

    @NotNull(message = "createdBy is required")
    private Long createdBy;

    @NotNull(message = "modifiedBy is required")
    private Long modifiedBy;
}
