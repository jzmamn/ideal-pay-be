package com.payroll.controller;

import com.payroll.dto.request.PayslipTemplateRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.PayslipTemplateResponseDTO;
import com.payroll.service.PayslipTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/payslip-template")
@RequiredArgsConstructor
public class PayslipTemplateController {

    private final PayslipTemplateService payslipTemplateService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<PayslipTemplateResponseDTO>>> getAllPayslipTemplates(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow,
            @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Payslip templates fetched successfully",
                payslipTemplateService.getAllPayslipTemplates(showDefaultRow, isActive)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponseDTO<PayslipTemplateResponseDTO>> getActive() {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Active payslip template fetched successfully",
                payslipTemplateService.getActive()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<PayslipTemplateResponseDTO>> getPayslipTemplateById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Payslip template fetched successfully",
                payslipTemplateService.getPayslipTemplateById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<PayslipTemplateResponseDTO>> createPayslipTemplate(
            @Valid @RequestBody PayslipTemplateRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Payslip template created successfully",
                        payslipTemplateService.createPayslipTemplate(requestDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<PayslipTemplateResponseDTO>> updatePayslipTemplate(
            @PathVariable Long id,
            @Valid @RequestBody PayslipTemplateRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Payslip template updated successfully",
                payslipTemplateService.updatePayslipTemplate(id, requestDTO)));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponseDTO<PayslipTemplateResponseDTO>> activate(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Payslip template activated successfully",
                payslipTemplateService.activate(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deletePayslipTemplate(@PathVariable Long id) {
        payslipTemplateService.deletePayslipTemplate(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Payslip template deleted successfully", null));
    }
}
