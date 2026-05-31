package com.payroll.service;

import com.payroll.dto.request.EmployeeProfileSaveRequestDTO;
import com.payroll.dto.response.EmployeePayrollComponentsResponseDTO;

public interface EmployeeProfileService {

    EmployeePayrollComponentsResponseDTO getEmployeeProfile(Long empId, boolean assignedOnly);

    EmployeePayrollComponentsResponseDTO saveEmployeeProfile(Long empId, EmployeeProfileSaveRequestDTO requestDTO);
}
