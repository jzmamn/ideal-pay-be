package com.payroll.service;

import com.payroll.dto.request.DistrictRequestDTO;
import com.payroll.dto.response.DistrictResponseDTO;

import java.util.List;

public interface DistrictService {

    List<DistrictResponseDTO> getAllDistricts(boolean showDefaultRow, String isActive);

    DistrictResponseDTO getDistrictById(Long id);

    DistrictResponseDTO createDistrict(DistrictRequestDTO requestDTO);

    DistrictResponseDTO updateDistrict(Long id, DistrictRequestDTO requestDTO);

    void deleteDistrict(Long id);
}
