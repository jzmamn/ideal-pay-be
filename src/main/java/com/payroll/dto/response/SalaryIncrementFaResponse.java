package com.payroll.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SalaryIncrementFaResponse {
    private Long id;
    private Long   faId;
    private String faCode;
    private String faName;
    private BigDecimal currentAmount;
    private BigDecimal incrementAmount;
    private BigDecimal newAmount;
    private Long   createdById;
    private String createdByCode;
    private String createdByUserName;
    private LocalDateTime createdDate;
    private Long   modifiedById;
    private String modifiedByCode;
    private String modifiedByUserName;
    private LocalDateTime modifiedDate;
}
