package com.payroll.dto.response;

import com.payroll.enums.BonusCalculationType;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonusCalculationResponseDTO {

    /** The bonus calculation type that was used. */
    private BonusCalculationType calculationType;

    /** Human-readable description of the formula applied. */
    private String expression;

    /** The computed bonus amount. Null when an error occurred. */
    private BigDecimal result;

    /** The context variables that were used during calculation. */
    private Map<String, Object> context;

    /**
     * Raw exception message for developer inspection.
     * Null when calculation succeeded.
     */
    private String technicalError;

    /**
     * Plain-language explanation of what went wrong.
     * Null when calculation succeeded.
     */
    private String userFriendlyError;
}
