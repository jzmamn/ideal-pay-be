package com.payroll.controller;

import com.payroll.dto.request.EmployeeNopayRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.EmployeeNopayResponseDTO;
import com.payroll.service.EmployeeNopayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/emp-np")
@RequiredArgsConstructor
public class EmployeeNopayController {

    private final EmployeeNopayService employeeNopayService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<EmployeeNopayResponseDTO>>> getAllEmployeeNopays(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee nopay records fetched successfully",
                employeeNopayService.getAllEmployeeNopays(showDefaultRow)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmployeeNopayResponseDTO>> getEmployeeNopayById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee nopay record fetched successfully",
                employeeNopayService.getEmployeeNopayById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<EmployeeNopayResponseDTO>> createEmployeeNopay(
            @Valid @RequestBody EmployeeNopayRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(
                "Employee nopay record created successfully",
                employeeNopayService.createEmployeeNopay(requestDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<EmployeeNopayResponseDTO>> updateEmployeeNopay(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeNopayRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee nopay record updated successfully",
                employeeNopayService.updateEmployeeNopay(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteEmployeeNopay(@PathVariable Long id) {
        employeeNopayService.deleteEmployeeNopay(id);
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee nopay record deleted successfully", null));
    }
}
