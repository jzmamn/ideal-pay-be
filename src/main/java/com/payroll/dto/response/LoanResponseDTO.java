package com.payroll.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoanResponseDTO {
    private Long id;
    private String code;
    private String name;
    private String isActive;
    private String description;
    private BigDecimal amount;
    private Long createdById;
    private String createdByUserName;
    private LocalDateTime createdDate;
    private Long modifiedById;
    private String modifiedByUserName;
    private LocalDateTime modifiedDate;
}
