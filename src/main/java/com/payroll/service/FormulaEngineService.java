package com.payroll.service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Core MVEL-based formula evaluation service.
 *
 * <p>Standard context variables available in every formula:
 * <ul>
 *   <li>{@code basicSalary}   — BigDecimal</li>
 *   <li>{@code workingDays}   — Integer</li>
 *   <li>{@code nopayDays}     — Integer</li>
 *   <li>{@code otHours}       — BigDecimal</li>
 *   <li>{@code otRate}        — BigDecimal</li>
 *   <li>Any additional keys from the caller's custom variable map</li>
 * </ul>
 */
public interface FormulaEngineService {

    /**
     * Evaluates a raw MVEL expression string with the given context.
     *
     * @param expression MVEL expression that must resolve to a numeric type
     * @param context    variable bindings exposed inside the expression
     * @return the computed result as BigDecimal
     * @throws com.payroll.exception.FormulaEvaluationException if the expression fails
     */
    BigDecimal evaluate(String expression, Map<String, Object> context);

    /**
     * Validates a MVEL expression by compiling it (no execution).
     *
     * @param expression the expression to validate
     * @return {@code null} if valid, or a technical error message if invalid
     */
    String validateExpression(String expression);

    /**
     * Converts a runtime formula exception into a plain-language message
     * that is safe to display in a UI or API response.
     *
     * <p>Covers the most common MVEL / JVM failure modes:
     * <ul>
     *   <li>Division by zero</li>
     *   <li>Undefined / unknown variable</li>
     *   <li>Null variable value</li>
     *   <li>Non-numeric return type</li>
     *   <li>Incompatible operand types</li>
     * </ul>
     *
     * @param ex the exception thrown during evaluation
     * @return a human-readable error string; never null
     */
    String toUserFriendlyMessage(Exception ex);
}
