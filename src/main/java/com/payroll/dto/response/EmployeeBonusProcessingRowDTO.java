package com.payroll.dto.response;

import com.payroll.enums.BonusStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** A single employee row within a bonus processing batch (detail view). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeBonusProcessingRowDTO {

    private Long id;

    // Employee
    private Long empId;
    private String empCode;
    private String empName;
    private String departmentName;
    private String designationName;
    private String branchName;

    // Amounts
    private BigDecimal calculatedAmount;   // amount stored in emp_bonus.amount
    private BigDecimal adjustedAmount;     // null if not adjusted
    private BigDecimal effectiveAmount;    // adjustedAmount ?? calculatedAmount

    // Formula audit
    private String formulaExpression;
    private BigDecimal formulaResult;

    // Status
    private BonusStatus status;

    // Approval
    private Long approvedById;
    private String approvedByUserName;
    private LocalDateTime approvedDate;

    private String note;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
