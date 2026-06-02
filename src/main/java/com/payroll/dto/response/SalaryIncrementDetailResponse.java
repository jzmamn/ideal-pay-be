package com.payroll.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SalaryIncrementDetailResponse {
    private Long id;
    private Long   incrementId;
    private Long   empId;
    private String empCode;
    private String empName;
    private String designationName;
    private String branchName;
    private BigDecimal currentBasic;
    private BigDecimal incrementBasic;
    private BigDecimal newBasic;
    private Boolean isExported;
    private LocalDateTime exportedDate;
    private String remarks;
    private List<SalaryIncrementFaResponse> faIncrements;
    private Long   createdById;
    private String createdByCode;
    private String createdByUserName;
    private LocalDateTime createdDate;
    private Long   modifiedById;
    private String modifiedByCode;
    private String modifiedByUserName;
    private LocalDateTime modifiedDate;
}
