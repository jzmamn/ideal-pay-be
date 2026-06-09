package com.payroll.service;

import com.payroll.dto.request.EmployeeLateRequestDTO;
import com.payroll.dto.response.EmployeeLateResponseDTO;

import java.util.List;

public interface EmployeeLateService {

    List<EmployeeLateResponseDTO> getAllEmployeeLates(boolean showDefaultRow);

    EmployeeLateResponseDTO getEmployeeLateById(Long id);

    EmployeeLateResponseDTO createEmployeeLate(EmployeeLateRequestDTO requestDTO);

    EmployeeLateResponseDTO updateEmployeeLate(Long id, EmployeeLateRequestDTO requestDTO);

    void deleteEmployeeLate(Long id);

    List<EmployeeLateResponseDTO> getByEmployeeId(Long empId);
}
