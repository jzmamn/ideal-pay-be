package com.payroll.dto.response;

import com.payroll.enums.PayrollRunStatus;
import com.payroll.enums.RunType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRunResponseDTO {

    private Long id;
    private String payrollMonth;
    private PayrollRunStatus status;
    private RunType runType;
    private Long parentRunId;

    // Employee
    private Long empId;
    private String empCode;
    private String empName;

    // Totals
    private BigDecimal basicSalary;
    private BigDecimal totalAllowances;
    private BigDecimal totalDeductions;
    private BigDecimal grossPay;
    private BigDecimal netPay;

    // Statutory
    private BigDecimal epfLiableBase;
    private BigDecimal taxableEarnings;
    private BigDecimal employeeEpf;
    private BigDecimal employerEpf;
    private BigDecimal etf;
    private BigDecimal payeTax;
    private Integer workingDays;

    // Processing info
    private LocalDateTime processedDate;
    private Long processedById;
    private String processedByUserName;

    // Detail lines
    private List<PayrollRunDetailResponseDTO> details;

    // Audit
    private Long createdById;
    private String createdByUserName;
    private LocalDateTime createdDate;
    private Long modifiedById;
    private String modifiedByUserName;
    private LocalDateTime modifiedDate;
}
