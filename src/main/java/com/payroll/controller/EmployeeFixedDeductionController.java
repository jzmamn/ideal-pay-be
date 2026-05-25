package com.payroll.controller;

import com.payroll.dto.request.EmployeeFixedDeductionRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.EmployeeFixedDeductionResponseDTO;
import com.payroll.service.EmployeeFixedDeductionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/emp-fd")
@RequiredArgsConstructor
public class EmployeeFixedDeductionController {

    private final EmployeeFixedDeductionService employeeFixedDeductionService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<EmployeeFixedDeductionResponseDTO>>> getAllEmployeeFixedDeductions(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee fixed deductions fetched successfully",
                employeeFixedDeductionService.getAllEmployeeFixedDeductions(showDefaultRow)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmployeeFixedDeductionResponseDTO>> getEmployeeFixedDeductionById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee fixed deduction fetched successfully",
                employeeFixedDeductionService.getEmployeeFixedDeductionById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<EmployeeFixedDeductionResponseDTO>> createEmployeeFixedDeduction(
            @Valid @RequestBody EmployeeFixedDeductionRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(
                "Employee fixed deduction created successfully",
                employeeFixedDeductionService.createEmployeeFixedDeduction(requestDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmployeeFixedDeductionResponseDTO>> updateEmployeeFixedDeduction(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeFixedDeductionRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee fixed deduction updated successfully",
                employeeFixedDeductionService.updateEmployeeFixedDeduction(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteEmployeeFixedDeduction(@PathVariable Long id) {
        employeeFixedDeductionService.deleteEmployeeFixedDeduction(id);
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee fixed deduction deleted successfully", null));
    }
}
