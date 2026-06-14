package com.payroll.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OvertimeResponseDTO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Boolean isActive;

    /** MVEL formula expression for this overtime type, if set. */
    private String formula;
    private Boolean formulaEnabled;

    /** Statutory liability flags. */
    private Boolean liableForEpf;
    private Boolean liableForEtf;
    private Boolean liableForPaye;
    private Boolean liableForNopay;

    private Long createdById;
    private String createdByCode;
    private String createdByUserName;
    private LocalDateTime createdDate;
    private Long modifiedById;
    private String modifiedByCode;
    private String modifiedByUserName;
    private LocalDateTime modifiedDate;
}
