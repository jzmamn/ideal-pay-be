package com.payroll.service;

import com.payroll.dto.request.EmployeeOvertimeRequestDTO;
import com.payroll.dto.response.EmployeeOvertimeResponseDTO;

import java.util.List;

public interface EmployeeOvertimeService {

    List<EmployeeOvertimeResponseDTO> getAllEmployeeOvertimes(boolean showDefaultRow);

    EmployeeOvertimeResponseDTO getEmployeeOvertimeById(Long id);

    EmployeeOvertimeResponseDTO createEmployeeOvertime(EmployeeOvertimeRequestDTO requestDTO);

    EmployeeOvertimeResponseDTO updateEmployeeOvertime(Long id, EmployeeOvertimeRequestDTO requestDTO);

    void deleteEmployeeOvertime(Long id);

    List<EmployeeOvertimeResponseDTO> getByEmployeeId(Long empId);
}
