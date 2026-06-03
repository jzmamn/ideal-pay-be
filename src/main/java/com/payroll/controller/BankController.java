package com.payroll.controller;

import com.payroll.dto.request.BankRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.BankResponseDTO;
import com.payroll.service.BankService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/bank")
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<BankResponseDTO>>> getAllBanks(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow,
            @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Banks fetched successfully",
                bankService.getAllBanks(showDefaultRow, isActive)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<BankResponseDTO>> getBankById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Bank fetched successfully",
                bankService.getBankById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<BankResponseDTO>> createBank(
            @Valid @RequestBody BankRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Bank created successfully",
                        bankService.createBank(requestDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<BankResponseDTO>> updateBank(
            @PathVariable Long id,
            @Valid @RequestBody BankRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Bank updated successfully",
                bankService.updateBank(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteBank(@PathVariable Long id) {
        bankService.deleteBank(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Bank deleted successfully", null));
    }
}
