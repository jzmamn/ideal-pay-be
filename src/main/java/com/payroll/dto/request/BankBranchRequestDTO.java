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
public class BankBranchRequestDTO {

    @NotBlank(message = "Bank code is required")
    @Size(max = 20, message = "Bank code must not exceed 20 characters")
    private String bankCode;

    @NotBlank(message = "Branch code is required")
    @Size(max = 20, message = "Branch code must not exceed 20 characters")
    private String branchCode;

    @NotBlank(message = "Branch name is required")
    @Size(max = 150, message = "Branch name must not exceed 150 characters")
    private String branchName;

    @NotNull(message = "createdBy is required")
    private Long createdBy;

    @NotNull(message = "modifiedBy is required")
    private Long modifiedBy;
}
