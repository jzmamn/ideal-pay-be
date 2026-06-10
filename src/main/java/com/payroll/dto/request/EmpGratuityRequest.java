package com.payroll.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmpGratuityRequest {

    @NotNull
    private Long empId;

    @NotNull
    private LocalDate terminationDate;

    @NotNull
    private LocalDate joinedDate;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal yearsOfService;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal basicSalary;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal gratuityAmount;

    private String remarks;

    @NotNull
    private Long createdBy;

    @NotNull
    private Long modifiedBy;
}
