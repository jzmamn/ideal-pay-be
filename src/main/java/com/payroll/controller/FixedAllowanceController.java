package com.payroll.controller;

import com.payroll.dto.request.FixedAllowanceRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.FixedAllowanceResponseDTO;
import com.payroll.service.FixedAllowanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/fixed-allowance")
@RequiredArgsConstructor
public class FixedAllowanceController {

    private final FixedAllowanceService fixedAllowanceService;

    // GET /payroll/fixed-allowance
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<FixedAllowanceResponseDTO>>> getAllFixedAllowances(@RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow, @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        List<FixedAllowanceResponseDTO> data = fixedAllowanceService.getAllFixedAllowances(showDefaultRow, isActive);
        return ResponseEntity.ok(ApiResponseDTO.success("Fixed allowances fetched successfully", data));
    }

    // GET /payroll/fixed-allowance/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<FixedAllowanceResponseDTO>> getFixedAllowanceById(
            @PathVariable Long id) {
        FixedAllowanceResponseDTO data = fixedAllowanceService.getFixedAllowanceById(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Fixed allowance fetched successfully", data));
    }

    // POST /payroll/fixed-allowance
    @PostMapping
    public ResponseEntity<ApiResponseDTO<FixedAllowanceResponseDTO>> createFixedAllowance(
            @Valid @RequestBody FixedAllowanceRequestDTO requestDTO) {
        FixedAllowanceResponseDTO data = fixedAllowanceService.createFixedAllowance(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Fixed allowance created successfully", data));
    }

    // PUT /payroll/fixed-allowance/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<FixedAllowanceResponseDTO>> updateFixedAllowance(
            @PathVariable Long id,
            @Valid @RequestBody FixedAllowanceRequestDTO requestDTO) {
        FixedAllowanceResponseDTO data = fixedAllowanceService.updateFixedAllowance(id, requestDTO);
        return ResponseEntity.ok(ApiResponseDTO.success("Fixed allowance updated successfully", data));
    }

    // DELETE /payroll/fixed-allowance/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteFixedAllowance(@PathVariable Long id) {
        fixedAllowanceService.deleteFixedAllowance(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Fixed allowance deleted successfully", null));
    }
}
