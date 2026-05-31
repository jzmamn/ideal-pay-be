package com.payroll.controller;

import com.payroll.dto.request.EmployeeProfileSaveRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.EmployeePayrollComponentsResponseDTO;
import com.payroll.service.EmployeeProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payroll/emp-profile")
@RequiredArgsConstructor
public class EmployeeProfileController {

    private final EmployeeProfileService employeeProfileService;

    @GetMapping("/{empId}")
    public ResponseEntity<ApiResponseDTO<EmployeePayrollComponentsResponseDTO>> getEmployeeProfile(
            @PathVariable Long empId,
            @RequestParam(value = "assignedOnly", defaultValue = "false") boolean assignedOnly) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee profile fetched successfully",
                employeeProfileService.getEmployeeProfile(empId, assignedOnly)));
    }

    @PostMapping("/{empId}")
    public ResponseEntity<ApiResponseDTO<EmployeePayrollComponentsResponseDTO>> saveEmployeeProfile(
            @PathVariable Long empId,
            @Valid @RequestBody EmployeeProfileSaveRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(
                "Employee profile saved successfully",
                employeeProfileService.saveEmployeeProfile(empId, requestDTO)));
    }
}
