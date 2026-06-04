package com.payroll.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MarkTransferredRequestDTO {

    @NotEmpty(message = "At least one run ID is required")
    private List<Long> runIds;

    @NotNull(message = "transferredBy is required")
    private Long transferredBy;
}
