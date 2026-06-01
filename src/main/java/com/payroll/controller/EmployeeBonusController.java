package com.payroll.controller;

import com.payroll.dto.request.EmployeeBonusRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.EmployeeBonusResponseDTO;
import com.payroll.service.EmployeeBonusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/emp-bonus")
@RequiredArgsConstructor
public class EmployeeBonusController {

    private final EmployeeBonusService service;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<EmployeeBonusResponseDTO>>> getAll(
            @RequestParam(defaultValue = "false") boolean showDefaultRow) {
        return ResponseEntity.ok(ApiResponseDTO.success("Employee bonuses fetched successfully", service.getAll(showDefaultRow)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmployeeBonusResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("Employee bonus fetched successfully", service.getById(id)));
    }

    @GetMapping("/employee/{empId}")
    public ResponseEntity<ApiResponseDTO<List<EmployeeBonusResponseDTO>>> getByEmployeeId(@PathVariable Long empId) {
        return ResponseEntity.ok(ApiResponseDTO.success("Employee bonuses fetched successfully", service.getByEmployeeId(empId)));
    }

    @GetMapping("/month/{payrollMonth}")
    public ResponseEntity<ApiResponseDTO<List<EmployeeBonusResponseDTO>>> getByPayrollMonth(@PathVariable String payrollMonth) {
        return ResponseEntity.ok(ApiResponseDTO.success("Employee bonuses fetched successfully", service.getByPayrollMonth(payrollMonth)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<EmployeeBonusResponseDTO>> create(@Valid @RequestBody EmployeeBonusRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success("Employee bonus created successfully", service.create(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmployeeBonusResponseDTO>> update(@PathVariable Long id, @Valid @RequestBody EmployeeBonusRequestDTO dto) {
        return ResponseEntity.ok(ApiResponseDTO.success("Employee bonus updated successfully", service.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Employee bonus deleted successfully", null));
    }
}
