package com.payroll.service.impl;

import com.payroll.config.SecurityContextHelper;
import com.payroll.dto.request.CountryRequestDTO;
import com.payroll.dto.response.CountryResponseDTO;
import com.payroll.entity.Country;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.CountryMapper;
import com.payroll.repository.CountryRepository;
import com.payroll.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;
    private final SecurityContextHelper securityContextHelper;

    @Override
    @Transactional(readOnly = true)
    public List<CountryResponseDTO> getAllCountries(boolean showDefaultRow, String isActive) {
        if (!isActive.equalsIgnoreCase("true") && !isActive.equalsIgnoreCase("false") && !isActive.equalsIgnoreCase("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("name").ascending();
        List<Country> records = "all".equals(isActive)
                ? countryRepository.findAll(sort)
                : countryRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(countryMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CountryResponseDTO getCountryById(Long id) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Country", "id", id));
        return countryMapper.toResponseDTO(country);
    }

    @Override
    public CountryResponseDTO createCountry(CountryRequestDTO requestDTO) {
        if (countryRepository.existsByIso2IgnoreCase(requestDTO.getIso2())) {
            throw new IllegalArgumentException(
                    "A country with ISO2 code '" + requestDTO.getIso2() + "' already exists.");
        }
        Country entity = countryMapper.toEntity(requestDTO);
        var currentUser = securityContextHelper.getCurrentUser();
        entity.setCreatedBy(currentUser);
        entity.setModifiedBy(currentUser);
        // Auto-generate code as CNT_<id>
        Country saved = countryRepository.save(entity);
        saved.setCode("CNT_" + saved.getId());
        return countryMapper.toResponseDTO(countryRepository.save(saved));
    }

    @Override
    public CountryResponseDTO updateCountry(Long id, CountryRequestDTO requestDTO) {
        Country existing = countryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Country", "id", id));
        countryMapper.updateEntityFromDTO(requestDTO, existing);
        existing.setModifiedBy(securityContextHelper.getCurrentUser());
        return countryMapper.toResponseDTO(countryRepository.save(existing));
    }

    @Override
    public void deleteCountry(Long id) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Country", "id", id));
        countryRepository.delete(country);
    }
}
