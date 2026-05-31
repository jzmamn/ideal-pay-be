package com.payroll.service;

import com.payroll.dto.request.EmployeeVariableDeductionRequestDTO;
import com.payroll.dto.response.EmployeeVariableDeductionResponseDTO;

import java.util.List;

public interface EmployeeVariableDeductionService {

    List<EmployeeVariableDeductionResponseDTO> getAllEmployeeVariableDeductions(boolean showDefaultRow);

    EmployeeVariableDeductionResponseDTO getEmployeeVariableDeductionById(Long id);

    EmployeeVariableDeductionResponseDTO createEmployeeVariableDeduction(EmployeeVariableDeductionRequestDTO requestDTO);

    EmployeeVariableDeductionResponseDTO updateEmployeeVariableDeduction(Long id, EmployeeVariableDeductionRequestDTO requestDTO);

    void deleteEmployeeVariableDeduction(Long id);

    List<EmployeeVariableDeductionResponseDTO> getByEmployeeId(Long empId);
}
