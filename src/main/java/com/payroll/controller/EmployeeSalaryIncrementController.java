package com.payroll.controller;

import com.payroll.dto.request.EmployeeSalaryIncrementRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.EmployeeSalaryIncrementResponseDTO;
import com.payroll.service.EmployeeSalaryIncrementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/emp-sal-incr")
@RequiredArgsConstructor
public class EmployeeSalaryIncrementController {

    private final EmployeeSalaryIncrementService service;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<EmployeeSalaryIncrementResponseDTO>>> getAll(
            @RequestParam(defaultValue = "false") boolean showDefaultRow) {
        return ResponseEntity.ok(ApiResponseDTO.success("Employee salary increments fetched successfully", service.getAll(showDefaultRow)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmployeeSalaryIncrementResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("Employee salary increment fetched successfully", service.getById(id)));
    }

    @GetMapping("/employee/{empId}")
    public ResponseEntity<ApiResponseDTO<List<EmployeeSalaryIncrementResponseDTO>>> getByEmployeeId(@PathVariable Long empId) {
        return ResponseEntity.ok(ApiResponseDTO.success("Employee salary increments fetched successfully", service.getByEmployeeId(empId)));
    }

    @GetMapping("/month/{payrollMonth}")
    public ResponseEntity<ApiResponseDTO<List<EmployeeSalaryIncrementResponseDTO>>> getByPayrollMonth(@PathVariable String payrollMonth) {
        return ResponseEntity.ok(ApiResponseDTO.success("Employee salary increments fetched successfully", service.getByPayrollMonth(payrollMonth)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<EmployeeSalaryIncrementResponseDTO>> create(@Valid @RequestBody EmployeeSalaryIncrementRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success("Employee salary increment created successfully", service.create(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmployeeSalaryIncrementResponseDTO>> update(@PathVariable Long id, @Valid @RequestBody EmployeeSalaryIncrementRequestDTO dto) {
        return ResponseEntity.ok(ApiResponseDTO.success("Employee salary increment updated successfully", service.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Employee salary increment deleted successfully", null));
    }
}
