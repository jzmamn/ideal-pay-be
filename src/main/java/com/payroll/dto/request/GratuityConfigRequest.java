package com.payroll.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GratuityConfigRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 255)
    private String description;

    private String  formula;
    private Boolean formulaEnabled = false;
    private Boolean isActive       = true;

    @NotNull
    private Long createdBy;

    @NotNull
    private Long modifiedBy;
}
