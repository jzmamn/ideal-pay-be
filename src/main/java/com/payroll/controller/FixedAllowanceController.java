package com.payroll.controller;

import com.payroll.dto.request.FixedAllowanceRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.FixedAllowanceResponseDTO;
import com.payroll.service.FixedAllowanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/payroll/fixed-allowance")
@RequiredArgsConstructor
public class FixedAllowanceController {

    private final FixedAllowanceService fixedAllowanceService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<FixedAllowanceResponseDTO>>> getAllFixedAllowances(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow,
            @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Fixed allowances fetched successfully",
                fixedAllowanceService.getAllFixedAllowances(showDefaultRow, isActive)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<FixedAllowanceResponseDTO>> getFixedAllowanceById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Fixed allowance fetched successfully",
                fixedAllowanceService.getFixedAllowanceById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<FixedAllowanceResponseDTO>> createFixedAllowance(
            @Valid @RequestBody FixedAllowanceRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Fixed allowance created successfully",
                        fixedAllowanceService.createFixedAllowance(requestDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<FixedAllowanceResponseDTO>> updateFixedAllowance(
            @PathVariable Long id,
            @Valid @RequestBody FixedAllowanceRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Fixed allowance updated successfully",
                fixedAllowanceService.updateFixedAllowance(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteFixedAllowance(@PathVariable Long id) {
        fixedAllowanceService.deleteFixedAllowance(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Fixed allowance deleted successfully", null));
    }
}
