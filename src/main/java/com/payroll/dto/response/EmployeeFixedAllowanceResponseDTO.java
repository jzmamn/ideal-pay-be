package com.payroll.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeFixedAllowanceResponseDTO {

    private Long id;
    private Boolean isAssigned;
    private BigDecimal amount;
    private String payrollMonth;
    private Boolean isProcessed;
    private LocalDateTime processedDate;

    private Long empId;
    private String empCode;
    private String empName;

    private Long faId;
    private String faCode;
    private String faName;

    private Long createdById;
    private String createdByCode;
    private String createdByUserName;
    private LocalDateTime createdDate;
    private Long modifiedById;
    private String modifiedByCode;
    private String modifiedByUserName;
    private LocalDateTime modifiedDate;
}
