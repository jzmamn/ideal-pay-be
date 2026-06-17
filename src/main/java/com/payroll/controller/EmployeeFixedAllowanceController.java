package com.payroll.controller;

import com.payroll.dto.request.EmployeeFixedAllowanceAssignRequestDTO;
import com.payroll.dto.request.EmployeeFixedAllowanceRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.EmployeeFixedAllowanceResponseDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
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

    /**
     * Employee → Salary Tab → Fixed Allowance checkbox grid.
     * Replaces the employee's Fixed Allowance assignments for the given payroll month with
     * exactly the selections sent — unselected allowances are removed, selected ones are
     * created or updated.
     */
    @PutMapping("/employee/{empId}/assign")
    public ResponseEntity<ApiResponseDTO<List<EmployeeFixedAllowanceResponseDTO>>> assignFixedAllowances(
            @PathVariable Long empId,
            @Valid @RequestBody EmployeeFixedAllowanceAssignRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee fixed allowances updated successfully",
                employeeFixedAllowanceService.assignFixedAllowances(empId, requestDTO)));
    }

    /**
     * Employee → Salary Tab → Fixed Allowance checkbox grid.
     * Computes the amount for one Fixed Allowance/employee pair (formula evaluated against the
     * employee's basicSalary + the payroll month's working days, or the static amount when no
     * formula is configured). Called the instant a checkbox is checked, so the row's amount can
     * be populated before the grid is saved.
     */
    @GetMapping("/employee/{empId}/preview-amount")
    public ResponseEntity<ApiResponseDTO<FormulaEvaluateResponseDTO>> previewAmount(
            @PathVariable Long empId,
            @RequestParam Long faId,
            @RequestParam(required = false) String payrollMonth) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Fixed allowance amount calculated successfully",
                employeeFixedAllowanceService.previewAmount(empId, faId, payrollMonth)));
    }
}
