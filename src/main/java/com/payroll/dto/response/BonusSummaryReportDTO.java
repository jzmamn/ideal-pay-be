package com.payroll.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BonusSummaryReportDTO {
    private String bonusCode;
    private String bonusName;
    private String payrollMonth;
    private String status;
    private Integer employeeCount;
    private BigDecimal totalAmount;
}
