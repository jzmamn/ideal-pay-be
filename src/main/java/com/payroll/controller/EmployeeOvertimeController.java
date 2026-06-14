package com.payroll.controller;

import com.payroll.dto.request.EmployeeOvertimeRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.EmployeeOvertimeResponseDTO;
import com.payroll.service.EmployeeOvertimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/emp-ot")
@RequiredArgsConstructor
public class EmployeeOvertimeController {

    private final EmployeeOvertimeService employeeOvertimeService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<EmployeeOvertimeResponseDTO>>> getAllEmployeeOvertimes(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee overtime records fetched successfully",
                employeeOvertimeService.getAllEmployeeOvertimes(showDefaultRow)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmployeeOvertimeResponseDTO>> getEmployeeOvertimeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee overtime record fetched successfully",
                employeeOvertimeService.getEmployeeOvertimeById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<EmployeeOvertimeResponseDTO>> createEmployeeOvertime(
            @Valid @RequestBody EmployeeOvertimeRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(
                "Employee overtime record created successfully",
                employeeOvertimeService.createEmployeeOvertime(requestDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmployeeOvertimeResponseDTO>> updateEmployeeOvertime(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeOvertimeRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee overtime record updated successfully",
                employeeOvertimeService.updateEmployeeOvertime(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteEmployeeOvertime(@PathVariable Long id) {
        employeeOvertimeService.deleteEmployeeOvertime(id);
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee overtime record deleted successfully", null));
    }

    /**
     * GET /payroll/emp-ot/by-employee?empId={empId}&payrollMonth={YYYY-MM}
     *
     * Returns all overtime records for a single employee in the given pay period.
     * Used by the individual payroll entry screen to display rate (read-only) and
     * allow the user to enter overtime hours.
     */
    @GetMapping("/by-employee")
    public ResponseEntity<ApiResponseDTO<List<EmployeeOvertimeResponseDTO>>> getByEmployee(
            @RequestParam Long empId,
            @RequestParam String payrollMonth) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee overtime records fetched successfully",
                employeeOvertimeService.getByEmployeeId(empId, payrollMonth)));
    }
}
