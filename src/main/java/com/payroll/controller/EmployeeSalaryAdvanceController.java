package com.payroll.controller;

import com.payroll.dto.request.EmployeeSalaryAdvanceRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.EmployeeSalaryAdvanceResponseDTO;
import com.payroll.service.EmployeeSalaryAdvanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/emp-sal-adv")
@RequiredArgsConstructor
public class EmployeeSalaryAdvanceController {

    private final EmployeeSalaryAdvanceService salaryAdvanceService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<EmployeeSalaryAdvanceResponseDTO>>> getAllSalaryAdvances(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee salary advances fetched successfully",
                salaryAdvanceService.getAllSalaryAdvances(showDefaultRow)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmployeeSalaryAdvanceResponseDTO>> getSalaryAdvanceById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee salary advance fetched successfully",
                salaryAdvanceService.getSalaryAdvanceById(id)));
    }

    @GetMapping("/employee/{empId}")
    public ResponseEntity<ApiResponseDTO<List<EmployeeSalaryAdvanceResponseDTO>>> getByEmployeeId(
            @PathVariable Long empId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee salary advances fetched successfully",
                salaryAdvanceService.getByEmployeeId(empId)));
    }

    @GetMapping("/month/{payrollMonth}")
    public ResponseEntity<ApiResponseDTO<List<EmployeeSalaryAdvanceResponseDTO>>> getByPayrollMonth(
            @PathVariable String payrollMonth) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee salary advances fetched successfully",
                salaryAdvanceService.getByPayrollMonth(payrollMonth)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<EmployeeSalaryAdvanceResponseDTO>> createSalaryAdvance(
            @Valid @RequestBody EmployeeSalaryAdvanceRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(
                "Employee salary advance created successfully",
                salaryAdvanceService.createSalaryAdvance(requestDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmployeeSalaryAdvanceResponseDTO>> updateSalaryAdvance(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeSalaryAdvanceRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee salary advance updated successfully",
                salaryAdvanceService.updateSalaryAdvance(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteSalaryAdvance(@PathVariable Long id) {
        salaryAdvanceService.deleteSalaryAdvance(id);
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee salary advance deleted successfully", null));
    }
}
