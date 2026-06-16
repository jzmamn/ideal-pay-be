package com.payroll.service;

import com.payroll.dto.request.EmployeeFixedDeductionAssignRequestDTO;
import com.payroll.dto.request.EmployeeFixedDeductionRequestDTO;
import com.payroll.dto.response.EmployeeFixedDeductionResponseDTO;

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
}
