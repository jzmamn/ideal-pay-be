package com.payroll.dto.request;

import com.payroll.enums.ComponentType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorrectionDetailUpdateDTO {

    /** The run-detail id to update (0 = new line to add) */
    private Long detailId;

    private ComponentType componentType;
    private Long componentId;
    private String componentCode;
    private String componentName;

    /** Corrected amount */
    private BigDecimal amount;

    /** OT hours (optional) */
    private BigDecimal hours;

    /** Nopay days (optional) */
    private BigDecimal days;
}
