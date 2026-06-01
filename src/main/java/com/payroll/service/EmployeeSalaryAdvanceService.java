package com.payroll.service;

import com.payroll.dto.request.EmployeeSalaryAdvanceRequestDTO;
import com.payroll.dto.response.EmployeeSalaryAdvanceResponseDTO;

import java.util.List;

public interface EmployeeSalaryAdvanceService {

    List<EmployeeSalaryAdvanceResponseDTO> getAllSalaryAdvances(boolean showDefaultRow);

    EmployeeSalaryAdvanceResponseDTO getSalaryAdvanceById(Long id);

    EmployeeSalaryAdvanceResponseDTO createSalaryAdvance(EmployeeSalaryAdvanceRequestDTO requestDTO);

    EmployeeSalaryAdvanceResponseDTO updateSalaryAdvance(Long id, EmployeeSalaryAdvanceRequestDTO requestDTO);

    void deleteSalaryAdvance(Long id);

    List<EmployeeSalaryAdvanceResponseDTO> getByEmployeeId(Long empId);

    List<EmployeeSalaryAdvanceResponseDTO> getByPayrollMonth(String payrollMonth);
}
