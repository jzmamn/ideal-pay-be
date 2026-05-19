package com.payroll.controller;

import com.payroll.dto.request.BranchRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.BranchResponseDTO;
import com.payroll.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/branch")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    // GET /payroll/branch
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<BranchResponseDTO>>> getAllBranches(@RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow, @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Branches fetched successfully",
                branchService.getAllBranches(showDefaultRow, isActive)));
    }

    // GET /payroll/branch/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<BranchResponseDTO>> getBranchById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Branch fetched successfully",
                branchService.getBranchById(id)));
    }

    // POST /payroll/branch
    @PostMapping
    public ResponseEntity<ApiResponseDTO<BranchResponseDTO>> createBranch(
            @Valid @RequestBody BranchRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Branch created successfully",
                        branchService.createBranch(requestDTO)));
    }

    // PUT /payroll/branch/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<BranchResponseDTO>> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody BranchRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Branch updated successfully",
                branchService.updateBranch(id, requestDTO)));
    }

    // DELETE /payroll/branch/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteBranch(@PathVariable Long id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Branch deleted successfully", null));
    }
}
