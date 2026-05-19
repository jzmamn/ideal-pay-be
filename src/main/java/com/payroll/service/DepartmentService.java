package com.payroll.service;

import com.payroll.dto.request.DepartmentRequestDTO;
import com.payroll.dto.response.DepartmentResponseDTO;

import java.util.List;

public interface DepartmentService {

    List<DepartmentResponseDTO> getAllDepartments(boolean showDefaultRow, String isActive);

    DepartmentResponseDTO getDepartmentById(Long id);

    DepartmentResponseDTO createDepartment(DepartmentRequestDTO requestDTO);

    DepartmentResponseDTO updateDepartment(Long id, DepartmentRequestDTO requestDTO);

    void deleteDepartment(Long id);
}
