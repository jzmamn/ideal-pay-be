package com.payroll.dto.response;

import com.payroll.enums.PeriodStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollPeriodResponseDTO {

    private Long id;
    private String periodMonth;
    private PeriodStatus status;
    private LocalDateTime closedDate;
    private String closedByUserName;
    private Long createdById;
    private String createdByUserName;
    private LocalDateTime createdDate;
    private Long modifiedById;
    private String modifiedByUserName;
    private LocalDateTime modifiedDate;
}
