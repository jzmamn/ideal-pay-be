package com.payroll.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedAllowanceResponseDTO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private BigDecimal amount;
    private Boolean isActive;
    private Boolean isTaxable;
    private Boolean liableForEpf;
    private Boolean liableForEtf;
    private Boolean liableForPaye;
    private Boolean liableNoPay;
    private Long createdById;
    private String createdByCode;
    private String createdByUserName;
    private LocalDateTime createdDate;
    private Long modifiedById;
    private String modifiedByCode;
    private String modifiedByUserName;
    private LocalDateTime modifiedDate;

}
