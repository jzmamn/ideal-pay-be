package com.payroll.controller;

import com.payroll.dto.request.CountryRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.CountryResponseDTO;
import com.payroll.service.CountryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/country")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;

    // GET /payroll/country
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<CountryResponseDTO>>> getAllCountries(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Countries fetched successfully",
                countryService.getAllCountries(showDefaultRow)));
    }

    // GET /payroll/country/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<CountryResponseDTO>> getCountryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Country fetched successfully",
                countryService.getCountryById(id)));
    }

    // POST /payroll/country
    @PostMapping
    public ResponseEntity<ApiResponseDTO<CountryResponseDTO>> createCountry(
            @Valid @RequestBody CountryRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Country created successfully",
                        countryService.createCountry(requestDTO)));
    }

    // PUT /payroll/country/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<CountryResponseDTO>> updateCountry(
            @PathVariable Long id,
            @Valid @RequestBody CountryRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Country updated successfully",
                countryService.updateCountry(id, requestDTO)));
    }

    // DELETE /payroll/country/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteCountry(@PathVariable Long id) {
        countryService.deleteCountry(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Country deleted successfully", null));
    }
}
