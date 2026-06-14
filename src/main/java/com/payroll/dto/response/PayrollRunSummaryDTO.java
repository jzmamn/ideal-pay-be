package com.payroll.dto.response;

import com.payroll.enums.PayrollRunStatus;
import com.payroll.enums.RunType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRunSummaryDTO {

    private Long id;
    private String payrollMonth;
    private PayrollRunStatus status;
    private RunType runType;
    private Long parentRunId;

    private Long empId;
    private String empCode;
    private String empName;

    private BigDecimal basicSalary;
    private BigDecimal totalAllowances;
    private BigDecimal totalDeductions;
    private BigDecimal grossPay;
    private BigDecimal netPay;

    private BigDecimal epfLiableBase;
    private BigDecimal taxableEarnings;
    private BigDecimal employeeEpf;
    private BigDecimal employerEpf;
    private BigDecimal etf;
    private BigDecimal payeTax;
    private Integer workingDays;
    private BigDecimal salaryAdvanceAmount;  // SA deduction for this run

    private LocalDateTime processedDate;
    private String processedByUserName;
}
