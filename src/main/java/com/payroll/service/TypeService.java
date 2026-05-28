package com.payroll.service;

import com.payroll.dto.request.TypeRequestDTO;
import com.payroll.dto.response.TypeResponseDTO;
import java.util.List;

public interface TypeService {

    List<TypeResponseDTO> getAllTypes(boolean showDefaultRow, String isActive);

    TypeResponseDTO getTypeById(Long id);

    TypeResponseDTO createType(TypeRequestDTO requestDTO);

    TypeResponseDTO updateType(Long id, TypeRequestDTO requestDTO);

    void deleteType(Long id);
}
