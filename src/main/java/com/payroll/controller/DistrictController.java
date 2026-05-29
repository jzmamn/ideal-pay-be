package com.payroll.controller;

import com.payroll.dto.request.DistrictRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.DistrictResponseDTO;
import com.payroll.service.DistrictService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/district")
@RequiredArgsConstructor
public class DistrictController {

    private final DistrictService districtService;

    // GET /payroll/district
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<DistrictResponseDTO>>> getAllDistricts(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow,
            @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Districts fetched successfully",
                districtService.getAllDistricts(showDefaultRow, isActive)));
    }

    // GET /payroll/district/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<DistrictResponseDTO>> getDistrictById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "District fetched successfully",
                districtService.getDistrictById(id)));
    }

    // POST /payroll/district
    @PostMapping
    public ResponseEntity<ApiResponseDTO<DistrictResponseDTO>> createDistrict(
            @Valid @RequestBody DistrictRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "District created successfully",
                        districtService.createDistrict(requestDTO)));
    }

    // PUT /payroll/district/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<DistrictResponseDTO>> updateDistrict(
            @PathVariable Long id,
            @Valid @RequestBody DistrictRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "District updated successfully",
                districtService.updateDistrict(id, requestDTO)));
    }

    // DELETE /payroll/district/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteDistrict(@PathVariable Long id) {
        districtService.deleteDistrict(id);
        return ResponseEntity.ok(ApiResponseDTO.success("District deleted successfully", null));
    }
}
