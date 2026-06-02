package com.payroll.dto.response;

import com.payroll.enums.IncrementStatus;
import com.payroll.enums.IncrementType;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SalaryIncrementResponse {
    private Long id;
    private String code;
    private String name;
    private IncrementType   type;
    private String          effectiveMonth;
    private IncrementStatus status;
    private String          remarks;
    private List<SalaryIncrementDetailResponse> details;
    private Long   createdById;
    private String createdByCode;
    private String createdByUserName;
    private LocalDateTime createdDate;
    private Long   modifiedById;
    private String modifiedByCode;
    private String modifiedByUserName;
    private LocalDateTime modifiedDate;
}
