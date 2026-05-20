package com.payroll.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariableDeductionResponseDTO {

    private Long id;
    private String code;
    private String name;
    private Boolean isActive;
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
