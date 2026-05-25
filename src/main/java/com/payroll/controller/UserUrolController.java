package com.payroll.controller;

import com.payroll.dto.request.UserUrolRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.UserUrolResponseDTO;
import com.payroll.service.UserUrolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/usr-urol")
@RequiredArgsConstructor
public class UserUrolController {

    private final UserUrolService userUrolService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<UserUrolResponseDTO>>> getAllUserRoles(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "User roles fetched successfully",
                userUrolService.getAllUserRoles(showDefaultRow)));
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<ApiResponseDTO<List<UserUrolResponseDTO>>> getUserRolesByUserId(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "User roles fetched successfully",
                userUrolService.getUserRolesByUserId(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<UserUrolResponseDTO>> getUserRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "User role fetched successfully",
                userUrolService.getUserRoleById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<UserUrolResponseDTO>> createUserRole(
            @Valid @RequestBody UserUrolRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "User role created successfully",
                        userUrolService.createUserRole(requestDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<UserUrolResponseDTO>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UserUrolRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "User role updated successfully",
                userUrolService.updateUserRole(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteUserRole(@PathVariable Long id) {
        userUrolService.deleteUserRole(id);
        return ResponseEntity.ok(ApiResponseDTO.success("User role deleted successfully", null));
    }
}
