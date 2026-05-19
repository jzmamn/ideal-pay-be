package com.payroll.service;

import com.payroll.dto.request.CompanyRequestDTO;
import com.payroll.dto.response.CompanyResponseDTO;

import java.util.List;

public interface CompanyService {

    List<CompanyResponseDTO> getAllCompanies(boolean showDefaultRow, String isActive);

    CompanyResponseDTO getCompanyById(Long id);

    CompanyResponseDTO createCompany(CompanyRequestDTO requestDTO);

    CompanyResponseDTO updateCompany(Long id, CompanyRequestDTO requestDTO);

    void deleteCompany(Long id);
}
