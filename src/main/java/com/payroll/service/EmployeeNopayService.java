package com.payroll.service;

import com.payroll.dto.request.EmployeeNopayRequestDTO;
import com.payroll.dto.response.EmployeeNopayResponseDTO;

import java.util.List;

public interface EmployeeNopayService {

    List<EmployeeNopayResponseDTO> getAllEmployeeNopays(boolean showDefaultRow);

    EmployeeNopayResponseDTO getEmployeeNopayById(Long id);

    EmployeeNopayResponseDTO createEmployeeNopay(EmployeeNopayRequestDTO requestDTO);

    EmployeeNopayResponseDTO updateEmployeeNopay(Long id, EmployeeNopayRequestDTO requestDTO);

    void deleteEmployeeNopay(Long id);

    List<EmployeeNopayResponseDTO> getByEmployeeId(Long empId);
}
