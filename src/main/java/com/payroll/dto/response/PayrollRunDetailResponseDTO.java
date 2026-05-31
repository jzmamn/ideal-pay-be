package com.payroll.dto.response;

import com.payroll.enums.ComponentType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRunDetailResponseDTO {

    private Long id;
    private ComponentType componentType;
    private Long componentId;
    private String componentCode;
    private String componentName;
    private BigDecimal amount;
    private BigDecimal hours;
    private BigDecimal days;
    private Long createdById;
    private String createdByUserName;
    private LocalDateTime createdDate;
    private Long modifiedById;
    private String modifiedByUserName;
    private LocalDateTime modifiedDate;
}
