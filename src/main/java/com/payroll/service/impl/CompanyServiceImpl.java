package com.payroll.service.impl;

import com.payroll.dto.request.CompanyRequestDTO;
import com.payroll.dto.response.CompanyResponseDTO;
import com.payroll.entity.Company;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.CompanyMapper;
import com.payroll.repository.CompanyRepository;
import com.payroll.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponseDTO> getAllCompanies(boolean showDefaultRow, String isActive) {
        if (!isActive.equals("true") && !isActive.equals("false") && !isActive.equals("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<Company> records = "all".equals(isActive)
                ? companyRepository.findAll(sort)
                : companyRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(companyMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponseDTO getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));
        return companyMapper.toResponseDTO(company);
    }

    @Override
    public CompanyResponseDTO createCompany(CompanyRequestDTO requestDTO) {
        if (companyRepository.existsByCodeIgnoreCase(requestDTO.getCode())) {
            throw new IllegalArgumentException(
                    "A company with code '" + requestDTO.getCode() + "' already exists.");
        }
        Company entity = companyMapper.toEntity(requestDTO);
        return companyMapper.toResponseDTO(companyRepository.save(entity));
    }

    @Override
    public CompanyResponseDTO updateCompany(Long id, CompanyRequestDTO requestDTO) {
        Company existing = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));
        companyMapper.updateEntityFromDTO(requestDTO, existing);
        return companyMapper.toResponseDTO(companyRepository.save(existing));
    }

    @Override
    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));
        companyRepository.delete(company);
    }
}
