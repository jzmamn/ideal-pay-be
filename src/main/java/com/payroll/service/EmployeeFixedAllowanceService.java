package com.payroll.service;

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
}
