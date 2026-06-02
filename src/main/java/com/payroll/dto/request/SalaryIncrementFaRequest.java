package com.payroll.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SalaryIncrementFaRequest {
    private Long   faId;
    private BigDecimal currentAmount   = BigDecimal.ZERO;
    private BigDecimal incrementAmount = BigDecimal.ZERO;
    private BigDecimal newAmount       = BigDecimal.ZERO;
    private Long   createdBy;
    private Long   modifiedBy;
}
