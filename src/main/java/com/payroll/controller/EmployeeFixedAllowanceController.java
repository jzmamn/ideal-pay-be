package com.payroll.controller;

import com.payroll.dto.request.EmployeeFixedAllowanceRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.EmployeeFixedAllowanceResponseDTO;
import com.payroll.service.EmployeeFixedAllowanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/emp-fa")
@RequiredArgsConstructor
public class EmployeeFixedAllowanceController {

    private final EmployeeFixedAllowanceService employeeFixedAllowanceService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<EmployeeFixedAllowanceResponseDTO>>> getAllEmployeeFixedAllowances(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee fixed allowances fetched successfully",
                employeeFixedAllowanceService.getAllEmployeeFixedAllowances(showDefaultRow)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmployeeFixedAllowanceResponseDTO>> getEmployeeFixedAllowanceById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee fixed allowance fetched successfully",
                employeeFixedAllowanceService.getEmployeeFixedAllowanceById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<EmployeeFixedAllowanceResponseDTO>> createEmployeeFixedAllowance(
            @Valid @RequestBody EmployeeFixedAllowanceRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(
                "Employee fixed allowance created successfully",
                employeeFixedAllowanceService.createEmployeeFixedAllowance(requestDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmployeeFixedAllowanceResponseDTO>> updateEmployeeFixedAllowance(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeFixedAllowanceRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee fixed allowance updated successfully",
                employeeFixedAllowanceService.updateEmployeeFixedAllowance(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteEmployeeFixedAllowance(@PathVariable Long id) {
        employeeFixedAllowanceService.deleteEmployeeFixedAllowance(id);
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee fixed allowance deleted successfully", null));
    }
}
