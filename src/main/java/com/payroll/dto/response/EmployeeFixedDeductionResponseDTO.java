package com.payroll.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeFixedDeductionResponseDTO {

    private Long id;
    private Boolean isAssigned;
    private BigDecimal amount;
    private String payrollMonth;
    private Boolean isProcessed;
    private LocalDateTime processedDate;

    private Long empId;
    private String empCode;
    private String empName;

    private Long fdId;
    private String fdCode;
    private String fdName;
    /**
     * True when {@code amount} was set by evaluating the deduction's MVEL formula at load time.
     * Formula-calculated amounts can only be changed by updating the deduction definition and re-loading.
     */
    private Boolean formulaCalculated;

    private Long createdById;
    private String createdByCode;
    private String createdByUserName;
    private LocalDateTime createdDate;
    private Long modifiedById;
    private String modifiedByCode;
    private String modifiedByUserName;
    private LocalDateTime modifiedDate;
}
