package com.payroll.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NopayDaysResponseDTO {

    private Long id;
    private String code;
    private String name;
    private BigDecimal days;
    private String description;
    private Boolean isActive;
    private String formula;
    private Boolean formulaEnabled;

    private Long createdById;
    private String createdByCode;
    private String createdByUserName;
    private LocalDateTime createdDate;
    private Long modifiedById;
    private String modifiedByCode;
    private String modifiedByUserName;
    private LocalDateTime modifiedDate;
}
