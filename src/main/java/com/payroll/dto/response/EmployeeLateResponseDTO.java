package com.payroll.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeLateResponseDTO {

    private Long id;
    /** Rate per hour set during load phase. Read-only in UI. */
    private BigDecimal rate;
    private BigDecimal hours;
    private BigDecimal amount;
    private String payrollMonth;
    private Boolean isProcessed;
    private LocalDateTime processedDate;

    private Long empId;
    private String empCode;
    private String empName;

    /** The LateDeductionConfig that generated the rate. */
    private Long   lateConfigId;
    private String lateConfigCode;
    private String lateConfigName;

    private Long createdById;
    private String createdByCode;
    private String createdByUserName;
    private LocalDateTime createdDate;
    private Long modifiedById;
    private String modifiedByCode;
    private String modifiedByUserName;
    private LocalDateTime modifiedDate;
}
