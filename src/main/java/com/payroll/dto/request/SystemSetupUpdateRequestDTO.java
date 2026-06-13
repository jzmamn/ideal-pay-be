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
public class SystemSetupUpdateRequestDTO {

    @NotBlank(message = "value is required")
    @Size(max = 255, message = "value must not exceed 255 characters")
    private String value;

    @NotNull(message = "isActive is required")
    private Boolean isActive;

    @NotNull(message = "modifiedBy is required")
    private Long modifiedBy;
}
