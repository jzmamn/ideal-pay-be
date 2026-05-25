package com.payroll.service.impl;

import com.payroll.exception.FormulaEvaluationException;
import com.payroll.service.FormulaEngineService;
import lombok.extern.slf4j.Slf4j;
import org.mvel2.MVEL;
import org.mvel2.CompileException;
import org.mvel2.PropertyAccessException;
import org.mvel2.compiler.CompiledExpression;
import org.mvel2.compiler.ExpressionCompiler;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MVEL-backed formula engine.
 *
 * <p>Compiled expressions are cached in a thread-safe map so that repeated
 * evaluation of the same formula pays the compilation cost only once.
 */
@Slf4j
@Service
public class FormulaEngineServiceImpl implements FormulaEngineService {

    private static final Pattern UNRESOLVABLE_PATTERN =
            Pattern.compile("unresolvable property or identifier[:\\s]+['\"]?(\\w+)['\"]?",
                    Pattern.CASE_INSENSITIVE);

    /** Cache of compiled MVEL expressions keyed by the raw expression string. */
    private final ConcurrentHashMap<String, Serializable> compiledCache = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    // evaluate
    // -------------------------------------------------------------------------

    @Override
    public BigDecimal evaluate(String expression, Map<String, Object> context) {
        if (expression == null || expression.isBlank()) {
            throw new FormulaEvaluationException("Formula expression must not be blank");
        }

        Serializable compiled = compiledCache.computeIfAbsent(expression, this::compile);

        Object raw;
        try {
            raw = MVEL.executeExpression(compiled, context);
        } catch (Exception ex) {
            log.error("MVEL evaluation error for expression [{}]: {}", expression, ex.getMessage());
            throw new FormulaEvaluationException(
                    "Error evaluating formula: " + ex.getMessage(), ex);
        }

        if (raw == null) {
            throw new FormulaEvaluationException(
                    "Formula evaluated to null. Check expression: " + expression);
        }

        return toBigDecimal(raw, expression);
    }

    // -------------------------------------------------------------------------
    // validateExpression
    // -------------------------------------------------------------------------

    @Override
    public String validateExpression(String expression) {
        if (expression == null || expression.isBlank()) {
            return "Expression must not be blank";
        }
        try {
            compile(expression);
            return null; // valid
        } catch (CompileException ex) {
            return "Compilation error: " + ex.getMessage();
        } catch (Exception ex) {
            return "Unexpected error: " + ex.getMessage();
        }
    }

    // -------------------------------------------------------------------------
    // toUserFriendlyMessage
    // -------------------------------------------------------------------------

    @Override
    public String toUserFriendlyMessage(Exception ex) {
        if (ex == null) return "An unknown formula error occurred.";

        Throwable root = rootCause(ex);
        String msg = root.getMessage() != null ? root.getMessage() : "";

        // Division by zero
        if (root instanceof ArithmeticException || msg.contains("/ by zero")) {
            return "Division by zero — ensure no divisor in the formula (e.g. workingDays) is zero.";
        }

        // MVEL undefined variable / unresolvable property
        if (root instanceof PropertyAccessException || msg.toLowerCase().contains("unresolvable")) {
            Matcher m = UNRESOLVABLE_PATTERN.matcher(msg);
            if (m.find()) {
                return "Unknown variable '" + m.group(1) + "' — make sure it is included in the request body.";
            }
            return "The formula references a variable that was not provided — check the request body for missing values.";
        }

        // Null value in expression
        if (root instanceof NullPointerException || msg.toLowerCase().contains("null")) {
            return "A formula variable is null — verify that all required values are included in the request.";
        }

        // Type mismatch / cast error
        if (root instanceof ClassCastException || msg.toLowerCase().contains("cannot cast")
                || msg.toLowerCase().contains("incompatible")) {
            return "Incompatible value types — all variables used in the formula must be numeric.";
        }

        // Formula returned null (our own check)
        if (msg.contains("evaluated to null")) {
            return "The formula returned null — verify the expression always produces a numeric result.";
        }

        // Formula returned non-numeric
        if (msg.contains("must return a numeric value")) {
            return "The formula returned a non-numeric value — the expression must evaluate to a number.";
        }

        // MVEL compile error (shouldn't reach here at runtime, but just in case)
        if (root instanceof CompileException) {
            return "The formula contains a syntax error — please review the expression.";
        }

        // Fallback
        return "Formula evaluation failed — review the expression and the values supplied.";
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Serializable compile(String expression) {
        ExpressionCompiler compiler = new ExpressionCompiler(expression);
        CompiledExpression compiled = compiler.compile();
        log.debug("Compiled MVEL expression: {}", expression);
        return compiled;
    }

    private BigDecimal toBigDecimal(Object value, String expression) {
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return new BigDecimal(n.toString());
        throw new FormulaEvaluationException(
                "Formula must return a numeric value but returned ["
                        + value.getClass().getSimpleName() + "] for expression: " + expression);
    }

    /** Unwrap nested causes to get to the root exception. */
    private Throwable rootCause(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }
}
