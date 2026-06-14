package com.payroll.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BonusDepartmentReportDTO {
    private String departmentName;
    private String bonusName;
    private String payrollMonth;
    private Integer employeeCount;
    private BigDecimal totalAmount;
}
