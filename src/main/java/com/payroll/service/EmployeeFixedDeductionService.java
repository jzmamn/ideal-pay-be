package com.payroll.service;

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
}
