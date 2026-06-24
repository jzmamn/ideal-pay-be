package com.payroll.controller;

import com.payroll.dto.request.LateDeductionConfigRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.dto.response.LateDeductionConfigResponseDTO;
import com.payroll.formula.PayrollContextBuilder;
import com.payroll.service.LateDeductionConfigService;
import com.payroll.service.SystemSetupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/payroll/late-deduction-config")
@RequiredArgsConstructor
public class LateDeductionConfigController {

    private final LateDeductionConfigService configService;
    private final SystemSetupService         systemSetupService;

    // GET /payroll/late-deduction-config
    @GetMapping
    public ResponseEntity<ApiResponseDTO<LateDeductionConfigResponseDTO>> get() {
        LateDeductionConfigResponseDTO data = configService.get();
        if (data == null) {
            return ResponseEntity.ok(ApiResponseDTO.success("No late deduction config found", null));
        }
        return ResponseEntity.ok(ApiResponseDTO.success("Late deduction config fetched successfully", data));
    }

    // PUT /payroll/late-deduction-config
    @PutMapping
    public ResponseEntity<ApiResponseDTO<LateDeductionConfigResponseDTO>> save(
            @Valid @RequestBody LateDeductionConfigRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Late deduction config saved successfully",
                configService.save(requestDTO)));
    }

    // POST /payroll/late-deduction-config/calculate
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponseDTO<FormulaEvaluateResponseDTO>> calculate(
            @RequestBody Map<String, Object> body) {

        Map<String, Object> context = PayrollContextBuilder.builder()
                .basicSalary(parseBD(body.get("basicSalary")))
                .workingDays(parseInt(body.get("workingDays"), systemSetupService.getWorkingDays()))
                .lateHours(parseBD(body.get("lateHours")))
                .build();

        return ResponseEntity.ok(ApiResponseDTO.success(
                "Late deduction amount calculated successfully",
                configService.calculateAmount(context)));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private BigDecimal parseBD(Object v) {
        if (v == null) return BigDecimal.ZERO;
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private int parseInt(Object v, int def) {
        if (v == null) return def;
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return def; }
    }
}
