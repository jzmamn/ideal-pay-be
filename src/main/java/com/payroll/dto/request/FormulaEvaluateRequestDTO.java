package com.payroll.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormulaEvaluateRequestDTO {

    /**
     * Optional: evaluate a saved formula by its ID.
     * If provided, the stored expression is used.
     */
    private Long formulaId;

    /**
     * Optional: evaluate an ad-hoc expression directly (used for testing).
     * Required when formulaId is not provided.
     */
    private String expression;

    // --- Standard payroll context variables ---

    private BigDecimal basicSalary;

    /** Total working days in the payroll period (e.g. 26) */
    private Integer workingDays;

    /** Number of no-pay days taken by the employee */
    private Integer nopayDays;

    /** Overtime hours worked */
    private BigDecimal otHours;

    /** Overtime rate multiplier (e.g. 1.5 for time-and-a-half) */
    private BigDecimal otRate;

    /**
     * Any additional custom variables to expose in the formula context.
     * Keys map directly to MVEL variable names.
     */
    private Map<String, Object> customVariables;
}
