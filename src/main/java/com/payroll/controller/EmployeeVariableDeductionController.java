package com.payroll.controller;

import com.payroll.dto.request.EmployeeVariableDeductionRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.EmployeeVariableDeductionResponseDTO;
import com.payroll.service.EmployeeVariableDeductionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/emp-vd")
@RequiredArgsConstructor
public class EmployeeVariableDeductionController {

    private final EmployeeVariableDeductionService employeeVariableDeductionService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<EmployeeVariableDeductionResponseDTO>>> getAllEmployeeVariableDeductions(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee variable deductions fetched successfully",
                employeeVariableDeductionService.getAllEmployeeVariableDeductions(showDefaultRow)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmployeeVariableDeductionResponseDTO>> getEmployeeVariableDeductionById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee variable deduction fetched successfully",
                employeeVariableDeductionService.getEmployeeVariableDeductionById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<EmployeeVariableDeductionResponseDTO>> createEmployeeVariableDeduction(
            @Valid @RequestBody EmployeeVariableDeductionRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(
                "Employee variable deduction created successfully",
                employeeVariableDeductionService.createEmployeeVariableDeduction(requestDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmployeeVariableDeductionResponseDTO>> updateEmployeeVariableDeduction(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeVariableDeductionRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee variable deduction updated successfully",
                employeeVariableDeductionService.updateEmployeeVariableDeduction(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteEmployeeVariableDeduction(@PathVariable Long id) {
        employeeVariableDeductionService.deleteEmployeeVariableDeduction(id);
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee variable deduction deleted successfully", null));
    }
}
