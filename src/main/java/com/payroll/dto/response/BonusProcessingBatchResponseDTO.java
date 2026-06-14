package com.payroll.dto.response;

import com.payroll.enums.BonusStatus;
import com.payroll.enums.BonusCalculationMethod;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BonusProcessingBatchResponseDTO {

    private Long id;
    private String payrollMonth;
    private BonusStatus status;
    private Integer employeeCount;
    private BigDecimal totalAmount;
    private String notes;

    // Bonus master
    private Long bonusId;
    private String bonusCode;
    private String bonusName;
    private BonusCalculationMethod calculationMethod;
    private BigDecimal fixedAmount;
    private Boolean formulaEnabled;
    private String formula;

    // Audit
    private Long createdById;
    private String createdByUserName;
    private LocalDateTime createdDate;

    private Long approvedById;
    private String approvedByUserName;
    private LocalDateTime approvedDate;

    private Long processedById;
    private String processedByUserName;
    private LocalDateTime processedDate;

    private Long modifiedById;
    private String modifiedByUserName;
    private LocalDateTime modifiedDate;

    /** Populated only when fetching a single batch (detail view). */
    private List<EmployeeBonusProcessingRowDTO> entries;
}
