package com.payroll.service;

import com.payroll.dto.request.EmpTypeRequestDTO;
import com.payroll.dto.response.EmpTypeResponseDTO;

import java.util.List;

public interface EmpTypeService {

    List<EmpTypeResponseDTO> getAllEmpTypes(boolean showDefaultRow, String isActive);

    EmpTypeResponseDTO getEmpTypeById(Long id);

    EmpTypeResponseDTO createEmpType(EmpTypeRequestDTO requestDTO);

    EmpTypeResponseDTO updateEmpType(Long id, EmpTypeRequestDTO requestDTO);

    void deleteEmpType(Long id);
}
