package com.payroll.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchSaveEntryDTO {

    /** Matches code column in master table (fa.code, fd.code, va.code, etc.) */
    @NotBlank(message = "Component code is required")
    private String componentCode;

    /** FA | FD | VA | VD | OT | NOPAY */
    @NotBlank(message = "Component type is required")
    private String componentType;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    /** Required for OT — number of overtime hours */
    private BigDecimal hours;

    /** Required for NOPAY — number of no-pay days */
    private BigDecimal days;
}
