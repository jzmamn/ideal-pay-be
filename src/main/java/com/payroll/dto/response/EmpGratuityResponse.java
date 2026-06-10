package com.payroll.dto.response;

import com.payroll.enums.GratuityStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmpGratuityResponse {

    private Long   id;
    private String code;

    // Employee summary
    private Long   empId;
    private String empCode;
    private String empName;
    private String designationName;
    private String branchName;

    private LocalDate  terminationDate;
    private LocalDate  joinedDate;
    private BigDecimal yearsOfService;
    private BigDecimal basicSalary;
    private BigDecimal gratuityAmount;

    private GratuityStatus status;
    private String         remarks;

    private Long   createdById;
    private String createdByCode;
    private String createdByUserName;
    private Long   modifiedById;
    private String modifiedByCode;
    private String modifiedByUserName;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
