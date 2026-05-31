package com.payroll.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchSaveRequestDTO {

    @NotNull(message = "Period month is required")
    @Min(value = 1,  message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer periodMonth;

    @NotNull(message = "Period year is required")
    @Min(value = 2000, message = "Year must be 2000 or later")
    private Integer periodYear;

    @NotNull(message = "Entries list is required")
    @Valid
    private List<BatchSaveEntryDTO> entries;
}
