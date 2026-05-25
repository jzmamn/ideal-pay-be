package com.payroll.controller;

import com.payroll.dto.request.EmployeeVariableAllowanceRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.EmployeeVariableAllowanceResponseDTO;
import com.payroll.service.EmployeeVariableAllowanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/emp-va")
@RequiredArgsConstructor
public class EmployeeVariableAllowanceController {

    private final EmployeeVariableAllowanceService employeeVariableAllowanceService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<EmployeeVariableAllowanceResponseDTO>>> getAllEmployeeVariableAllowances(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee variable allowances fetched successfully",
                employeeVariableAllowanceService.getAllEmployeeVariableAllowances(showDefaultRow)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmployeeVariableAllowanceResponseDTO>> getEmployeeVariableAllowanceById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee variable allowance fetched successfully",
                employeeVariableAllowanceService.getEmployeeVariableAllowanceById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<EmployeeVariableAllowanceResponseDTO>> createEmployeeVariableAllowance(
            @Valid @RequestBody EmployeeVariableAllowanceRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(
                "Employee variable allowance created successfully",
                employeeVariableAllowanceService.createEmployeeVariableAllowance(requestDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmployeeVariableAllowanceResponseDTO>> updateEmployeeVariableAllowance(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeVariableAllowanceRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee variable allowance updated successfully",
                employeeVariableAllowanceService.updateEmployeeVariableAllowance(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteEmployeeVariableAllowance(@PathVariable Long id) {
        employeeVariableAllowanceService.deleteEmployeeVariableAllowance(id);
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee variable allowance deleted successfully", null));
    }
}
