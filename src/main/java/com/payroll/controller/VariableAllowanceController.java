package com.payroll.controller;

import com.payroll.dto.request.VariableAllowanceRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.dto.response.VariableAllowanceResponseDTO;
import com.payroll.formula.PayrollContextBuilder;
import com.payroll.service.VariableAllowanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payroll/variable-allowance")
@RequiredArgsConstructor
public class VariableAllowanceController {

    private final VariableAllowanceService variableAllowanceService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<VariableAllowanceResponseDTO>>> getAllVariableAllowances(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow,
            @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Variable allowances fetched successfully",
                variableAllowanceService.getAllVariableAllowances(showDefaultRow, isActive)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<VariableAllowanceResponseDTO>> getVariableAllowanceById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Variable allowance fetched successfully",
                variableAllowanceService.getVariableAllowanceById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<VariableAllowanceResponseDTO>> createVariableAllowance(
            @Valid @RequestBody VariableAllowanceRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Variable allowance created successfully",
                        variableAllowanceService.createVariableAllowance(requestDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<VariableAllowanceResponseDTO>> updateVariableAllowance(
            @PathVariable Long id,
            @Valid @RequestBody VariableAllowanceRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Variable allowance updated successfully",
                variableAllowanceService.updateVariableAllowance(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteVariableAllowance(@PathVariable Long id) {
        variableAllowanceService.deleteVariableAllowance(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Variable allowance deleted successfully", null));
    }

    /**
     * POST /payroll/variable-allowance/{id}/calculate
     * Evaluates the linked formula for this allowance type with the supplied payroll context.
     */
    @PostMapping("/{id}/calculate")
    public ResponseEntity<ApiResponseDTO<FormulaEvaluateResponseDTO>> calculateAmount(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {

        Map<String, Object> raw = body != null ? body : Map.of();
        @SuppressWarnings("unchecked")
        Map<String, Object> custom = raw.get("customVariables") instanceof Map
                ? (Map<String, Object>) raw.get("customVariables") : Map.of();

        Map<String, Object> ctx = PayrollContextBuilder.builder()
                .basicSalary(toBD(raw.get("basicSalary")))
                .workingDays(toInt(raw.get("workingDays"), 26))
                .nopayDays(toInt(raw.get("nopayDays"), 0))
                .otHours(toBD(raw.get("otHours")))
                .otRate(raw.get("otRate") != null ? toBD(raw.get("otRate")) : BigDecimal.ONE)
                .customVariables(custom)
                .build();

        return ResponseEntity.ok(ApiResponseDTO.success(
                "Variable allowance amount calculated successfully",
                variableAllowanceService.calculateAmount(id, ctx)));
    }

    private BigDecimal toBD(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private int toInt(Object v, int def) {
        if (v == null) return def;
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return def; }
    }
}
