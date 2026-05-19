package com.payroll.service;

import com.payroll.dto.request.NopayDaysRequestDTO;
import com.payroll.dto.response.NopayDaysResponseDTO;

import java.util.List;

public interface NopayDaysService {

    List<NopayDaysResponseDTO> getAllNopayDays(boolean showDefaultRow, String isActive);

    NopayDaysResponseDTO getNopayDaysById(Long id);

    NopayDaysResponseDTO createNopayDays(NopayDaysRequestDTO requestDTO);

    NopayDaysResponseDTO updateNopayDays(Long id, NopayDaysRequestDTO requestDTO);

    void deleteNopayDays(Long id);
}
