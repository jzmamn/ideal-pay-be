package com.payroll.controller;

import com.payroll.dto.request.EmployeeLoanRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.EmployeeLoanResponseDTO;
import com.payroll.service.EmployeeLoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/emp-loan")
@RequiredArgsConstructor
public class EmployeeLoanController {

    private final EmployeeLoanService service;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<EmployeeLoanResponseDTO>>> getAll(
            @RequestParam(defaultValue = "false") boolean showDefaultRow) {
        return ResponseEntity.ok(ApiResponseDTO.success("Employee loans fetched successfully", service.getAll(showDefaultRow)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmployeeLoanResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("Employee loan fetched successfully", service.getById(id)));
    }

    @GetMapping("/employee/{empId}")
    public ResponseEntity<ApiResponseDTO<List<EmployeeLoanResponseDTO>>> getByEmployeeId(@PathVariable Long empId) {
        return ResponseEntity.ok(ApiResponseDTO.success("Employee loans fetched successfully", service.getByEmployeeId(empId)));
    }

    @GetMapping("/month/{payrollMonth}")
    public ResponseEntity<ApiResponseDTO<List<EmployeeLoanResponseDTO>>> getByPayrollMonth(@PathVariable String payrollMonth) {
        return ResponseEntity.ok(ApiResponseDTO.success("Employee loans fetched successfully", service.getByPayrollMonth(payrollMonth)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<EmployeeLoanResponseDTO>> create(@Valid @RequestBody EmployeeLoanRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success("Employee loan created successfully", service.create(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmployeeLoanResponseDTO>> update(@PathVariable Long id, @Valid @RequestBody EmployeeLoanRequestDTO dto) {
        return ResponseEntity.ok(ApiResponseDTO.success("Employee loan updated successfully", service.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Employee loan deleted successfully", null));
    }
}
