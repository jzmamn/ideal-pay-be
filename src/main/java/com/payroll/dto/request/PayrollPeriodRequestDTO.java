package com.payroll.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollPeriodRequestDTO {

    @NotNull(message = "Company is required")
    private Long companyId;

    @NotNull(message = "Period year is required")
    @Min(value = 2000, message = "Period year must be 2000 or later")
    @Max(value = 2100, message = "Period year must be 2100 or earlier")
    private Integer periodYear;

    @NotNull(message = "Period month is required")
    @Min(value = 1, message = "Period month must be between 1 and 12")
    @Max(value = 12, message = "Period month must be between 1 and 12")
    private Integer periodMonth;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    /** Optional — defaults to 26 when omitted. */
    @Min(value = 1, message = "Working days must be at least 1")
    @Max(value = 31, message = "Working days cannot exceed 31")
    private Integer workingDays;

    @JsonIgnore
    @AssertTrue(message = "End date must be after start date")
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) return true; // covered by @NotNull
        return endDate.isAfter(startDate);
    }
}
