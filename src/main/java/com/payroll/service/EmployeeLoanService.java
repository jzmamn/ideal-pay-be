package com.payroll.service;

import com.payroll.dto.request.EmployeeLoanRequestDTO;
import com.payroll.dto.response.EmployeeLoanResponseDTO;
import java.util.List;

public interface EmployeeLoanService {
    List<EmployeeLoanResponseDTO> getAll(boolean showDefaultRow);
    EmployeeLoanResponseDTO getById(Long id);
    EmployeeLoanResponseDTO create(EmployeeLoanRequestDTO requestDTO);
    EmployeeLoanResponseDTO update(Long id, EmployeeLoanRequestDTO requestDTO);
    void delete(Long id);
    List<EmployeeLoanResponseDTO> getByEmployeeId(Long empId);
    List<EmployeeLoanResponseDTO> getByPayrollMonth(String payrollMonth);
}
