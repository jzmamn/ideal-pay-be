package com.payroll.service;

import com.payroll.dto.request.EmployeeSalaryIncrementRequestDTO;
import com.payroll.dto.response.EmployeeSalaryIncrementResponseDTO;
import java.util.List;

public interface EmployeeSalaryIncrementService {
    List<EmployeeSalaryIncrementResponseDTO> getAll(boolean showDefaultRow);
    EmployeeSalaryIncrementResponseDTO getById(Long id);
    EmployeeSalaryIncrementResponseDTO create(EmployeeSalaryIncrementRequestDTO requestDTO);
    EmployeeSalaryIncrementResponseDTO update(Long id, EmployeeSalaryIncrementRequestDTO requestDTO);
    void delete(Long id);
    List<EmployeeSalaryIncrementResponseDTO> getByEmployeeId(Long empId);
    List<EmployeeSalaryIncrementResponseDTO> getByPayrollMonth(String payrollMonth);
}
