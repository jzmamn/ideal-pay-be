package com.payroll.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SalaryIncrementDetailRequest {
    private Long   empId;
    private BigDecimal currentBasic   = BigDecimal.ZERO;
    private BigDecimal incrementBasic = BigDecimal.ZERO;
    private BigDecimal newBasic       = BigDecimal.ZERO;
    private String remarks;
    private Long   createdBy;
    private Long   modifiedBy;
    private List<SalaryIncrementFaRequest> faIncrements;
}
