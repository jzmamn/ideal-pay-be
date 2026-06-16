package com.payroll.enums;

/**
 * Bonus amounts are calculated exclusively via MVEL formula evaluation.
 * FIXED_AMOUNT was removed — fixed bonuses must be expressed as a literal
 * formula (e.g. {@code "50000"}).
 */
public enum BonusCalculationMethod {
    FORMULA_BASED
}
