package com.payroll.dto.response;

import lombok.*;

import java.math.BigDecimal;

/** One row in the Approval Report — a single batch with its approval status. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BonusApprovalReportDTO {
    private Long   batchId;
    private String bonusCode;
    private String bonusName;
    private String payrollMonth;
    private String status;
    private Integer employeeCount;
    private BigDecimal totalAmount;
    private String approvedByUserName;
    private String approvedDate;
    private String createdByUserName;
    private String createdDate;
}
