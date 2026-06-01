package com.payroll.service;

import com.payroll.dto.request.EmployeeBonusRequestDTO;
import com.payroll.dto.response.EmployeeBonusResponseDTO;
import java.util.List;

public interface EmployeeBonusService {
    List<EmployeeBonusResponseDTO> getAll(boolean showDefaultRow);
    EmployeeBonusResponseDTO getById(Long id);
    EmployeeBonusResponseDTO create(EmployeeBonusRequestDTO requestDTO);
    EmployeeBonusResponseDTO update(Long id, EmployeeBonusRequestDTO requestDTO);
    void delete(Long id);
    List<EmployeeBonusResponseDTO> getByEmployeeId(Long empId);
    List<EmployeeBonusResponseDTO> getByPayrollMonth(String payrollMonth);
}
