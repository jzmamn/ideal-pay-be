package com.payroll.service;

import com.payroll.dto.request.EmployeeFixedAllowanceAssignRequestDTO;
import com.payroll.dto.request.EmployeeFixedAllowanceRequestDTO;
import com.payroll.dto.response.EmployeeFixedAllowanceResponseDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;

import java.util.List;

public interface EmployeeFixedAllowanceService {

    List<EmployeeFixedAllowanceResponseDTO> getAllEmployeeFixedAllowances(boolean showDefaultRow);

    EmployeeFixedAllowanceResponseDTO getEmployeeFixedAllowanceById(Long id);

    EmployeeFixedAllowanceResponseDTO createEmployeeFixedAllowance(EmployeeFixedAllowanceRequestDTO requestDTO);

    EmployeeFixedAllowanceResponseDTO updateEmployeeFixedAllowance(Long id, EmployeeFixedAllowanceRequestDTO requestDTO);

    void deleteEmployeeFixedAllowance(Long id);

    List<EmployeeFixedAllowanceResponseDTO> getByEmployeeId(Long empId);
    List<EmployeeFixedAllowanceResponseDTO> getByEmployeeId(Long empId, String payrollMonth);

    /**
     * Replaces the employee's Fixed Allowance assignments for a single payroll month with
     * exactly the set passed in {@code requestDTO.selections}. Allowances previously assigned
     * for this employee/month but not present in the selection are deleted; selected allowances
     * are created or updated. Used by the Employee &rarr; Salary Tab &rarr; Fixed Allowance
     * checkbox grid.
     */
    List<EmployeeFixedAllowanceResponseDTO> assignFixedAllowances(Long empId, EmployeeFixedAllowanceAssignRequestDTO requestDTO);

    /**
     * Computes the amount this employee would receive for a given Fixed Allowance, by evaluating
     * the allowance's MVEL formula against the employee's basicSalary and the payroll month's
     * configured working days. Falls back to the Fixed Allowance's static {@code amount} when no
     * formula is configured. Used by the Employee &rarr; Salary Tab &rarr; Fixed Allowance
     * checkbox grid to populate the amount the instant a row is checked.
     */
    FormulaEvaluateResponseDTO previewAmount(Long empId, Long faId, String payrollMonth);
}
