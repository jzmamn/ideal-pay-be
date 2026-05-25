package com.payroll.exception;

public class FormulaEvaluationException extends RuntimeException {

    public FormulaEvaluationException(String message) {
        super(message);
    }

    public FormulaEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }
}
