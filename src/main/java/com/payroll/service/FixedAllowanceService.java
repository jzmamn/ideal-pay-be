package com.payroll.service;

import com.payroll.dto.request.FixedAllowanceRequestDTO;
import com.payroll.dto.response.FixedAllowanceResponseDTO;
import java.util.List;

public interface FixedAllowanceService {

    List<FixedAllowanceResponseDTO> getAllFixedAllowances(boolean showDefaultRow, String isActive);

    FixedAllowanceResponseDTO getFixedAllowanceById(Long id);

    FixedAllowanceResponseDTO createFixedAllowance(FixedAllowanceRequestDTO requestDTO);

    FixedAllowanceResponseDTO updateFixedAllowance(Long id, FixedAllowanceRequestDTO requestDTO);

    void deleteFixedAllowance(Long id);
}
