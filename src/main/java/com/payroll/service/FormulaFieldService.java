package com.payroll.service;

import com.payroll.dto.response.FormulaFieldDTO;

import java.util.List;

public interface FormulaFieldService {

    /**
     * Returns all variables available for use inside a formula expression.
     * Includes static fields (e.g. basicSalary, workingDays) and dynamic fields
     * derived from active FixedAllowance, VariableAllowance, FixedDeduction,
     * VariableDeduction, NopayDays, and Overtime codes.
     */
    List<FormulaFieldDTO> getAllFormulaFields();
}
