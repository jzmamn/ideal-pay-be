package com.payroll.enums;

public enum ComponentType {
    FA,
    FD,
    VA,
    VD,
    OT,
    NOPAY,
    EPF_EE,   // Employee EPF deduction (8%)
    EPF_ER,   // Employer EPF contribution (12%) — for reporting only
    ETF,      // ETF contribution (3%) — employer only, for reporting
    PAYE,     // PAYE income tax deduction
    SA        // Salary advance deduction
}
