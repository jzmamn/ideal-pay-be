package com.payroll.controller;

import com.payroll.dto.request.VariableDeductionRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.VariableDeductionResponseDTO;
import com.payroll.service.VariableDeductionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/variable-deduction")
@RequiredArgsConstructor
public class VariableDeductionController {

    private final VariableDeductionService variableDeductionService;

    // GET /payroll/variable-deduction
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<VariableDeductionResponseDTO>>> getAllVariableDeductions(@RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow, @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Variable deductions fetched successfully",
                variableDeductionService.getAllVariableDeductions(showDefaultRow, isActive)));
    }

    // GET /payroll/variable-deduction/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<VariableDeductionResponseDTO>> getVariableDeductionById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Variable deduction fetched successfully",
                variableDeductionService.getVariableDeductionById(id)));
    }

    // POST /payroll/variable-deduction
    @PostMapping
    public ResponseEntity<ApiResponseDTO<VariableDeductionResponseDTO>> createVariableDeduction(
            @Valid @RequestBody VariableDeductionRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Variable deduction created successfully",
                        variableDeductionService.createVariableDeduction(requestDTO)));
    }

    // PUT /payroll/variable-deduction/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<VariableDeductionResponseDTO>> updateVariableDeduction(
            @PathVariable Long id,
            @Valid @RequestBody VariableDeductionRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Variable deduction updated successfully",
                variableDeductionService.updateVariableDeduction(id, requestDTO)));
    }

    // DELETE /payroll/variable-deduction/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteVariableDeduction(@PathVariable Long id) {
        variableDeductionService.deleteVariableDeduction(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Variable deduction deleted successfully", null));
    }
}
