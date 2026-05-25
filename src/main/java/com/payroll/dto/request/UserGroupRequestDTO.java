package com.payroll.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGroupRequestDTO {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "grpId is required")
    private Long grpId;

    @NotNull(message = "isActive is required")
    private Boolean isActive;

    @NotNull(message = "createdBy is required")
    private Long createdBy;

    @NotNull(message = "modifiedBy is required")
    private Long modifiedBy;
}
