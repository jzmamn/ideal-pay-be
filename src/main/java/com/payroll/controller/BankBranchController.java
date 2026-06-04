package com.payroll.controller;

import com.payroll.dto.request.BankBranchRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.BankBranchResponseDTO;
import com.payroll.service.BankBranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/bank-branch")
@RequiredArgsConstructor
public class BankBranchController {

    private final BankBranchService bankBranchService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<BankBranchResponseDTO>>> getAllBankBranches(
            @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Bank branches fetched successfully",
                bankBranchService.getAllBankBranches(isActive)));
    }

    @GetMapping("/by-bank/{bankCode}")
    public ResponseEntity<ApiResponseDTO<List<BankBranchResponseDTO>>> getBankBranchesByBankCode(
            @PathVariable String bankCode) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Bank branches fetched successfully",
                bankBranchService.getBankBranchesByBankCode(bankCode)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<BankBranchResponseDTO>> getBankBranchById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Bank branch fetched successfully",
                bankBranchService.getBankBranchById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<BankBranchResponseDTO>> createBankBranch(
            @Valid @RequestBody BankBranchRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Bank branch created successfully",
                        bankBranchService.createBankBranch(requestDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<BankBranchResponseDTO>> updateBankBranch(
            @PathVariable Long id,
            @Valid @RequestBody BankBranchRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Bank branch updated successfully",
                bankBranchService.updateBankBranch(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteBankBranch(@PathVariable Long id) {
        bankBranchService.deleteBankBranch(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Bank branch deleted successfully", null));
    }
}
