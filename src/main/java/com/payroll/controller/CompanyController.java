package com.payroll.controller;

import com.payroll.dto.request.CompanyRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.CompanyResponseDTO;
import com.payroll.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    // GET /payroll/company
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<CompanyResponseDTO>>> getAllCompanies(@RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow, @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Companies fetched successfully",
                companyService.getAllCompanies(showDefaultRow, isActive)));
    }

    // GET /payroll/company/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<CompanyResponseDTO>> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Company fetched successfully",
                companyService.getCompanyById(id)));
    }

    // POST /payroll/company
    @PostMapping
    public ResponseEntity<ApiResponseDTO<CompanyResponseDTO>> createCompany(
            @Valid @RequestBody CompanyRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Company created successfully",
                        companyService.createCompany(requestDTO)));
    }

    // PUT /payroll/company/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<CompanyResponseDTO>> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Company updated successfully",
                companyService.updateCompany(id, requestDTO)));
    }

    // DELETE /payroll/company/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Company deleted successfully", null));
    }
}
