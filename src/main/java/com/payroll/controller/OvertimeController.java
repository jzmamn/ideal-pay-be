package com.payroll.controller;

import com.payroll.dto.request.OvertimeRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.dto.response.OvertimeResponseDTO;
import com.payroll.formula.PayrollContextBuilder;
import com.payroll.service.OvertimeService;
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
@RequestMapping("/payroll/overtime")
@RequiredArgsConstructor
public class OvertimeController {

    private final OvertimeService overtimeService;
    private final SystemSetupService systemSetupService;

    // GET /payroll/overtime
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<OvertimeResponseDTO>>> getAllOvertimes(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow,
            @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Overtimes fetched successfully",
                overtimeService.getAllOvertimes(showDefaultRow, isActive)));
    }

    // GET /payroll/overtime/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<OvertimeResponseDTO>> getOvertimeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Overtime fetched successfully",
                overtimeService.getOvertimeById(id)));
    }

    // POST /payroll/overtime
    @PostMapping
    public ResponseEntity<ApiResponseDTO<OvertimeResponseDTO>> createOvertime(
            @Valid @RequestBody OvertimeRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Overtime created successfully",
                        overtimeService.createOvertime(requestDTO)));
    }

    // PUT /payroll/overtime/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<OvertimeResponseDTO>> updateOvertime(
            @PathVariable Long id,
            @Valid @RequestBody OvertimeRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Overtime updated successfully",
                overtimeService.updateOvertime(id, requestDTO)));
    }

    // DELETE /payroll/overtime/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteOvertime(@PathVariable Long id) {
        overtimeService.deleteOvertime(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Overtime deleted successfully", null));
    }

    /**
     * POST /payroll/overtime/{id}/calculate
     *
     * Computes the overtime amount for an employee using the linked formula (or fixed amount fallback).
     *
     * Request body example:
     * {
     *   "basicSalary": 80000,
     *   "workingDays": 26,
     *   "otHours": 12,
     *   "otRate": 1.5,
     *   "customVariables": {}
     * }
     */
    @PostMapping("/{id}/calculate")
    public ResponseEntity<ApiResponseDTO<FormulaEvaluateResponseDTO>> calculateOvertimeAmount(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        Map<String, Object> context = PayrollContextBuilder.builder()
                .basicSalary(parseBigDecimal(body.get("basicSalary")))
                .workingDays(parseInteger(body.get("workingDays"), systemSetupService.getWorkingDays()))
                .nopayDays(parseInteger(body.get("nopayDays"), 0))
                .otHours(parseBigDecimal(body.get("otHours")))
                .otRate(parseBigDecimal(body.get("otRate")))
                .customVariables(parseCustomVariables(body.get("customVariables")))
                .build();

        return ResponseEntity.ok(ApiResponseDTO.success(
                "Overtime amount calculated successfully",
                overtimeService.calculateAmount(id, context)));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) return null;
        return new BigDecimal(value.toString());
    }

    private Integer parseInteger(Object value, int defaultValue) {
        if (value == null) return defaultValue;
        return Integer.parseInt(value.toString());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseCustomVariables(Object value) {
        if (value instanceof Map) return (Map<String, Object>) value;
        return null;
    }
}
