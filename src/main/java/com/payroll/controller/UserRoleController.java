package com.payroll.controller;

import com.payroll.dto.request.UserRoleRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.UserRoleResponseDTO;
import com.payroll.service.UserRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/user-roles")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    // GET /payroll/user-roles
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<UserRoleResponseDTO>>> getAllUserRoles(@RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow, @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "User roles fetched successfully",
                userRoleService.getAllUserRoles(showDefaultRow, isActive)));
    }

    // GET /payroll/user-roles/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<UserRoleResponseDTO>> getUserRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "User role fetched successfully",
                userRoleService.getUserRoleById(id)));
    }

    // POST /payroll/user-roles
    @PostMapping
    public ResponseEntity<ApiResponseDTO<UserRoleResponseDTO>> createUserRole(
            @Valid @RequestBody UserRoleRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "User role created successfully",
                        userRoleService.createUserRole(requestDTO)));
    }

    // PUT /payroll/user-roles/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<UserRoleResponseDTO>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "User role updated successfully",
                userRoleService.updateUserRole(id, requestDTO)));
    }

    // DELETE /payroll/user-roles/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteUserRole(@PathVariable Long id) {
        userRoleService.deleteUserRole(id);
        return ResponseEntity.ok(ApiResponseDTO.success("User role deleted successfully", null));
    }
}
