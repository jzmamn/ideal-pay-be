package com.payroll.dto.response;

import com.payroll.enums.PayrollStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollPeriodResponseDTO {

    private Long id;
    private Long companyId;
    private String companyName;
    private Integer periodYear;
    private Integer periodMonth;
    private String periodCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer workingDays;
    private PayrollStatus payrollStatus;
    private Boolean locked;
    private Boolean active;
    private LocalDate payrollRunDate;
    private LocalDateTime closedDate;
    private String closedByUserName;
    private Long createdById;
    private String createdByUserName;
    private LocalDateTime createdDate;
    private Long modifiedById;
    private String modifiedByUserName;
    private LocalDateTime modifiedDate;
}
