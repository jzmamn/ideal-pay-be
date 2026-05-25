package com.payroll.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormulaEvaluateResponseDTO {

    /** The expression that was evaluated. */
    private String expression;

    /** The numeric result of the MVEL evaluation. Null when an error occurred. */
    private BigDecimal result;

    /** The context variables that were used during evaluation. */
    private Map<String, Object> context;

    /**
     * Raw MVEL / JVM exception message.
     * Intended for developers and log inspection.
     * Null when evaluation succeeded.
     */
    private String technicalError;

    /**
     * Plain-language explanation of what went wrong.
     * Suitable for display in a UI or API consumer message.
     * Null when evaluation succeeded.
     */
    private String userFriendlyError;
}
