package com.payroll.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedDeductionResponseDTO {

    private Long id;
    private String code;
    private String name;
    private BigDecimal amount;
    private Boolean isActive;
    private Boolean liableForEpf;
    private Boolean liableForEtf;
    private Boolean liableForPaye;
    private Boolean liableNoPay;
    private Long createdBy;
    private LocalDateTime createdDate;
    private Long modifiedBy;
    private LocalDateTime modifiedDate;
}
