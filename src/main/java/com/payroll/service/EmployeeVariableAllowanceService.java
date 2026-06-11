package com.payroll.service;

import com.payroll.dto.request.EmployeeVariableAllowanceRequestDTO;
import com.payroll.dto.response.EmployeeVariableAllowanceResponseDTO;

import java.util.List;

public interface EmployeeVariableAllowanceService {

    List<EmployeeVariableAllowanceResponseDTO> getAllEmployeeVariableAllowances(boolean showDefaultRow);

    EmployeeVariableAllowanceResponseDTO getEmployeeVariableAllowanceById(Long id);

    EmployeeVariableAllowanceResponseDTO createEmployeeVariableAllowance(EmployeeVariableAllowanceRequestDTO requestDTO);

    EmployeeVariableAllowanceResponseDTO updateEmployeeVariableAllowance(Long id, EmployeeVariableAllowanceRequestDTO requestDTO);

    void deleteEmployeeVariableAllowance(Long id);

    List<EmployeeVariableAllowanceResponseDTO> getByEmployeeId(Long empId);
    List<EmployeeVariableAllowanceResponseDTO> getByEmployeeId(Long empId, String payrollMonth);
}
