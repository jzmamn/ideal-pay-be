package com.payroll.controller;

import com.payroll.dto.request.FixedDeductionRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.FixedDeductionResponseDTO;
import com.payroll.service.FixedDeductionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/fixed-deduction")
@RequiredArgsConstructor
public class FixedDeductionController {

    private final FixedDeductionService fixedDeductionService;

    // GET /payroll/fixed-deduction
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<FixedDeductionResponseDTO>>> getAllFixedDeductions(@RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow, @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Fixed deductions fetched successfully",
                fixedDeductionService.getAllFixedDeductions(showDefaultRow, isActive)));
    }

    // GET /payroll/fixed-deduction/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<FixedDeductionResponseDTO>> getFixedDeductionById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Fixed deduction fetched successfully",
                fixedDeductionService.getFixedDeductionById(id)));
    }

    // POST /payroll/fixed-deduction
    @PostMapping
    public ResponseEntity<ApiResponseDTO<FixedDeductionResponseDTO>> createFixedDeduction(
            @Valid @RequestBody FixedDeductionRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Fixed deduction created successfully",
                        fixedDeductionService.createFixedDeduction(requestDTO)));
    }

    // PUT /payroll/fixed-deduction/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<FixedDeductionResponseDTO>> updateFixedDeduction(
            @PathVariable Long id,
            @Valid @RequestBody FixedDeductionRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Fixed deduction updated successfully",
                fixedDeductionService.updateFixedDeduction(id, requestDTO)));
    }

    // DELETE /payroll/fixed-deduction/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteFixedDeduction(@PathVariable Long id) {
        fixedDeductionService.deleteFixedDeduction(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Fixed deduction deleted successfully", null));
    }
}
