package com.payroll.service;

import com.payroll.dto.request.EmployeeFixedAllowanceAssignRequestDTO;
import com.payroll.dto.request.EmployeeFixedAllowanceRequestDTO;
import com.payroll.dto.response.EmployeeFixedAllowanceResponseDTO;

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
}
