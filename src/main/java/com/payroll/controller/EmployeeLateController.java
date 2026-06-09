package com.payroll.controller;

import com.payroll.dto.request.EmployeeLateRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.EmployeeLateResponseDTO;
import com.payroll.service.EmployeeLateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/emp-late")
@RequiredArgsConstructor
public class EmployeeLateController {

    private final EmployeeLateService employeeLateService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<EmployeeLateResponseDTO>>> getAllEmployeeLates(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee late records fetched successfully",
                employeeLateService.getAllEmployeeLates(showDefaultRow)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmployeeLateResponseDTO>> getEmployeeLateById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee late record fetched successfully",
                employeeLateService.getEmployeeLateById(id)));
    }

    @GetMapping("/employee/{empId}")
    public ResponseEntity<ApiResponseDTO<List<EmployeeLateResponseDTO>>> getByEmployee(@PathVariable Long empId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee late records fetched successfully",
                employeeLateService.getByEmployeeId(empId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<EmployeeLateResponseDTO>> createEmployeeLate(
            @Valid @RequestBody EmployeeLateRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(
                "Employee late record created successfully",
                employeeLateService.createEmployeeLate(requestDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmployeeLateResponseDTO>> updateEmployeeLate(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeLateRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee late record updated successfully",
                employeeLateService.updateEmployeeLate(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteEmployeeLate(@PathVariable Long id) {
        employeeLateService.deleteEmployeeLate(id);
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee late record deleted successfully", null));
    }
}
