package com.payroll.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BankTransferRowDTO {
    private Long runId;
    private Long empId;
    private String employeeNo;
    private String empName;
    private Long bankId;
    private String bankCode;
    private String bankName;
    private String branchCode;
    private String accountNo;
    private BigDecimal salaryAmount;
    private BigDecimal salaryAdvanceAmount;
    private BigDecimal fixedAllowanceAmount;
    private BigDecimal totalAmount;
    private String transferStatus;   // PENDING | TRANSFERRED
    private LocalDateTime transferredAt;
}
