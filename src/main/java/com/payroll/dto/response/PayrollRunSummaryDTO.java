package com.payroll.dto.response;

import com.payroll.enums.PayrollRunStatus;
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

    private Long empId;
    private String empCode;
    private String empName;

    private BigDecimal basicSalary;
    private BigDecimal totalAllowances;
    private BigDecimal totalDeductions;
    private BigDecimal grossPay;
    private BigDecimal netPay;

    private LocalDateTime processedDate;
    private String processedByUserName;
}
