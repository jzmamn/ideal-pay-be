package com.payroll.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/** Used for approve / process / cancel actions on a BonusProcessingBatch. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BonusBatchActionRequestDTO {

    @NotNull(message = "Acting user ID is required")
    private Long actingUserId;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
