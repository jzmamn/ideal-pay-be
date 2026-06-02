package com.payroll.service;

import com.payroll.dto.request.SalaryIncrementRequest;
import com.payroll.dto.response.SalaryIncrementResponse;
import com.payroll.enums.IncrementStatus;
import com.payroll.enums.IncrementType;

import java.util.List;

public interface SalaryIncrementService {

    List<SalaryIncrementResponse> getAll();

    List<SalaryIncrementResponse> getByType(IncrementType type);

    List<SalaryIncrementResponse> getByStatus(IncrementStatus status);

    List<SalaryIncrementResponse> getByEffectiveMonth(String month);

    List<SalaryIncrementResponse> getByEmployee(Long empId);

    SalaryIncrementResponse getById(Long id);

    SalaryIncrementResponse create(SalaryIncrementRequest request);

    SalaryIncrementResponse update(Long id, SalaryIncrementRequest request);

    void delete(Long id);

    /** DRAFT → APPROVED */
    SalaryIncrementResponse approve(Long id);

    /** APPROVED → CANCELLED */
    SalaryIncrementResponse cancel(Long id);

    /**
     * Export: apply increment to payroll.
     * Updates employee.basicSalary and emp_fa amounts for the effective month.
     * Status → EXPORTED.
     */
    SalaryIncrementResponse exportToPayroll(Long id);

    /**
     * Import: refresh currentBasic and currentAmount from live payroll data.
     * Recalculates newBasic / newAmount.
     */
    SalaryIncrementResponse importFromPayroll(Long id);
}
