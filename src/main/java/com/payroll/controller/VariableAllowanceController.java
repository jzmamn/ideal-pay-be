package com.payroll.controller;

import com.payroll.dto.request.VariableAllowanceRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.VariableAllowanceResponseDTO;
import com.payroll.service.VariableAllowanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/variable-allowance")
@RequiredArgsConstructor
public class VariableAllowanceController {

    private final VariableAllowanceService variableAllowanceService;

    // GET /payroll/variable-allowance
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<VariableAllowanceResponseDTO>>> getAllVariableAllowances(@RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow, @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Variable allowances fetched successfully",
                variableAllowanceService.getAllVariableAllowances(showDefaultRow, isActive)));
    }

    // GET /payroll/variable-allowance/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<VariableAllowanceResponseDTO>> getVariableAllowanceById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Variable allowance fetched successfully",
                variableAllowanceService.getVariableAllowanceById(id)));
    }

    // POST /payroll/variable-allowance
    @PostMapping
    public ResponseEntity<ApiResponseDTO<VariableAllowanceResponseDTO>> createVariableAllowance(
            @Valid @RequestBody VariableAllowanceRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Variable allowance created successfully",
                        variableAllowanceService.createVariableAllowance(requestDTO)));
    }

    // PUT /payroll/variable-allowance/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<VariableAllowanceResponseDTO>> updateVariableAllowance(
            @PathVariable Long id,
            @Valid @RequestBody VariableAllowanceRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Variable allowance updated successfully",
                variableAllowanceService.updateVariableAllowance(id, requestDTO)));
    }

    // DELETE /payroll/variable-allowance/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteVariableAllowance(@PathVariable Long id) {
        variableAllowanceService.deleteVariableAllowance(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Variable allowance deleted successfully", null));
    }
}
