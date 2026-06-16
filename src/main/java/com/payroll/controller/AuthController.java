package com.payroll.controller;

import com.payroll.dto.request.LoginRequestDTO;
import com.payroll.dto.request.RefreshRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.LoginResponseDTO;
import com.payroll.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payroll/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.success("Login successful", authService.login(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDTO<LoginResponseDTO>> refresh(
            @Valid @RequestBody RefreshRequestDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.success("Token refreshed", authService.refresh(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDTO<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.ok(ApiResponseDTO.success("Logged out", null));
    }
}
