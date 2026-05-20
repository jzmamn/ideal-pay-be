package com.payroll.service;

import com.payroll.dto.request.CountryRequestDTO;
import com.payroll.dto.response.CountryResponseDTO;

import java.util.List;

public interface CountryService {

    List<CountryResponseDTO> getAllCountries(boolean showDefaultRow);

    CountryResponseDTO getCountryById(Long id);

    CountryResponseDTO createCountry(CountryRequestDTO requestDTO);

    CountryResponseDTO updateCountry(Long id, CountryRequestDTO requestDTO);

    void deleteCountry(Long id);
}
