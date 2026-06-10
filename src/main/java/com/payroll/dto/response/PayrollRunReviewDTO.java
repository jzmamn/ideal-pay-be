package com.payroll.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRunReviewDTO {

    /** Employee display code (employee_no). */
    private String empId;

    /** Full name — first_name + last_name. */
    private String empName;

    /** FA | FD | VA | VD | OT | NOPAY | SA */
    private String componentType;

    /** Short code from the component master table. */
    private String componentCode;

    /** Human-readable label for the component. */
    private String componentName;

    /**
     * Y if a formula is configured on the component master, N otherwise.
     * When Y the engine may have computed a different value than entered.
     */
    private String hasFormula;

    /**
     * The formula expression string (informational, can be null).
     * Only present when hasFormula = Y.
     */
    private String formulaExpr;

    /** Amount entered during the batch input step. */
    private BigDecimal beforeValue;

    /** Amount calculated by the payroll engine. */
    private BigDecimal afterValue;

    /** afterValue − beforeValue. Positive = engine increased, negative = engine reduced. */
    private BigDecimal difference;

    /** Run status: DRAFT | LOCKED | CORRECTION_DRAFT | CORRECTION_LOCKED */
    private String runStatus;
}
