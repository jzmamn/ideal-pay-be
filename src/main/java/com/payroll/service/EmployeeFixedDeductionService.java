package com.payroll.service;

import com.payroll.dto.request.EmployeeFixedDeductionAssignRequestDTO;
import com.payroll.dto.request.EmployeeFixedDeductionRequestDTO;
import com.payroll.dto.response.EmployeeFixedDeductionResponseDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;

import java.util.List;

public interface EmployeeFixedDeductionService {

    List<EmployeeFixedDeductionResponseDTO> getAllEmployeeFixedDeductions(boolean showDefaultRow);

    EmployeeFixedDeductionResponseDTO getEmployeeFixedDeductionById(Long id);

    EmployeeFixedDeductionResponseDTO createEmployeeFixedDeduction(EmployeeFixedDeductionRequestDTO requestDTO);

    EmployeeFixedDeductionResponseDTO updateEmployeeFixedDeduction(Long id, EmployeeFixedDeductionRequestDTO requestDTO);

    void deleteEmployeeFixedDeduction(Long id);

    List<EmployeeFixedDeductionResponseDTO> getByEmployeeId(Long empId);
    List<EmployeeFixedDeductionResponseDTO> getByEmployeeId(Long empId, String payrollMonth);

    /**
     * Replaces the employee's Fixed Deduction assignments for a single payroll month with
     * exactly the set passed in {@code requestDTO.selections}. Deductions previously assigned
     * for this employee/month but not present in the selection are deleted; selected deductions
     * are created or updated. Used by the Employee &rarr; Salary Tab &rarr; Fixed Deduction
     * checkbox grid.
     */
    List<EmployeeFixedDeductionResponseDTO> assignFixedDeductions(Long empId, EmployeeFixedDeductionAssignRequestDTO requestDTO);

    /**
     * Computes the amount this employee would receive for a given Fixed Deduction, by evaluating
     * the deduction's MVEL formula against the employee's basicSalary and the payroll month's
     * configured working days. Fixed Deductions have no static fallback amount — if no formula is
     * configured, the result is zero. Used by the Employee &rarr; Salary Tab &rarr; Fixed Deduction
     * checkbox grid to populate the amount the instant a row is checked.
     */
    FormulaEvaluateResponseDTO previewAmount(Long empId, Long fdId, String payrollMonth);
}
