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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payroll/late-deduction-config")
@RequiredArgsConstructor
public class LateDeductionConfigController {

    private final LateDeductionConfigService configService;
    private final SystemSetupService systemSetupService;

    // GET /payroll/late-deduction-config
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<LateDeductionConfigResponseDTO>>> getAll(
            @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Late deduction configs fetched successfully",
                configService.getAll(isActive)));
    }

    // GET /payroll/late-deduction-config/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<LateDeductionConfigResponseDTO>> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Late deduction config fetched successfully",
                configService.getById(id)));
    }

    // POST /payroll/late-deduction-config
    @PostMapping
    public ResponseEntity<ApiResponseDTO<LateDeductionConfigResponseDTO>> create(
            @Valid @RequestBody LateDeductionConfigRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Late deduction config created successfully",
                        configService.create(requestDTO)));
    }

    // PUT /payroll/late-deduction-config/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<LateDeductionConfigResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody LateDeductionConfigRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Late deduction config updated successfully",
                configService.update(id, requestDTO)));
    }

    // DELETE /payroll/late-deduction-config/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable Long id) {
        configService.delete(id);
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Late deduction config deleted successfully", null));
    }

    /**
     * POST /payroll/late-deduction-config/{id}/calculate
     * Test the formula with sample values.
     * Body: { "basicSalary": 120000, "lateHours": 2 }
     */
    @PostMapping("/{id}/calculate")
    public ResponseEntity<ApiResponseDTO<FormulaEvaluateResponseDTO>> calculate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        Map<String, Object> context = PayrollContextBuilder.builder()
                .basicSalary(parseBD(body.get("basicSalary")))
                .workingDays(parseInt(body.get("workingDays"), systemSetupService.getWorkingDays()))
                .lateHours(parseBD(body.get("lateHours")))
                .build();

        return ResponseEntity.ok(ApiResponseDTO.success(
                "Late deduction amount calculated successfully",
                configService.calculateAmount(id, context)));
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
