package com.payroll.controller;

import com.payroll.dto.response.ActiveCodesResponseDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.service.CodeDropdownService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payroll/codes")
@RequiredArgsConstructor
public class CodeDropdownController {

    private final CodeDropdownService codeDropdownService;

    @GetMapping("/active")
    public ResponseEntity<ApiResponseDTO<ActiveCodesResponseDTO>> getActiveCodes() {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Active codes fetched successfully",
                codeDropdownService.getActiveCodes()));
    }
}
