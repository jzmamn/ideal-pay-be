package com.payroll.controller;

import com.payroll.dto.request.UrolRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.UrolResponseDTO;
import com.payroll.service.UrolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/urol")
@RequiredArgsConstructor
public class UrolController {

    private final UrolService urolService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<UrolResponseDTO>>> getAllRoles(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow,
            @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Roles fetched successfully",
                urolService.getAllRoles(showDefaultRow, isActive)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<UrolResponseDTO>> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Role fetched successfully",
                urolService.getRoleById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<UrolResponseDTO>> createRole(
            @Valid @RequestBody UrolRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Role created successfully",
                        urolService.createRole(requestDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<UrolResponseDTO>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UrolRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Role updated successfully",
                urolService.updateRole(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteRole(@PathVariable Long id) {
        urolService.deleteRole(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Role deleted successfully", null));
    }
}
