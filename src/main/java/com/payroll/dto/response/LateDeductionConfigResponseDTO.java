package com.payroll.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LateDeductionConfigResponseDTO {

    private Long    id;
    private String  code;
    private String  name;
    private String  description;
    private Integer workingDays;
    private Integer workingHoursPerDay;
    private String  formula;
    private Boolean formulaEnabled;
    private Boolean isActive;

    private Boolean liableForEpf;
    private Boolean liableForEtf;
    private Boolean liableForPaye;
    private Boolean liableForNopay;

    private Long          createdById;
    private String        createdByCode;
    private String        createdByUserName;
    private LocalDateTime createdDate;
    private Long          modifiedById;
    private String        modifiedByCode;
    private String        modifiedByUserName;
    private LocalDateTime modifiedDate;
}
