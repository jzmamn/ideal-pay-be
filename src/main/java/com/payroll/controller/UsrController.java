package com.payroll.controller;

import com.payroll.dto.request.UsrRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.UsrResponseDTO;
import com.payroll.service.UsrService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/users")
@RequiredArgsConstructor
public class UsrController {

    private final UsrService usrService;

    // GET /payroll/users
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<UsrResponseDTO>>> getAllUsers(@RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow, @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Users fetched successfully",
                usrService.getAllUsers(showDefaultRow, isActive)));
    }

    // GET /payroll/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<UsrResponseDTO>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "User fetched successfully",
                usrService.getUserById(id)));
    }

    // POST /payroll/users
    @PostMapping
    public ResponseEntity<ApiResponseDTO<UsrResponseDTO>> createUser(
            @Valid @RequestBody UsrRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "User created successfully",
                        usrService.createUser(requestDTO)));
    }

    // PUT /payroll/users/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<UsrResponseDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UsrRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "User updated successfully",
                usrService.updateUser(id, requestDTO)));
    }

    // PATCH /payroll/users/{id}/password
    @PatchMapping("/{id}/password")
    public ResponseEntity<ApiResponseDTO<Void>> updatePassword(
            @PathVariable Long id,
            @RequestParam @NotBlank(message = "New password is required") String newPassword) {
        usrService.updatePassword(id, newPassword);
        return ResponseEntity.ok(ApiResponseDTO.success("Password updated successfully", null));
    }

    // DELETE /payroll/users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteUser(@PathVariable Long id) {
        usrService.deleteUser(id);
        return ResponseEntity.ok(ApiResponseDTO.success("User deleted successfully", null));
    }
}
