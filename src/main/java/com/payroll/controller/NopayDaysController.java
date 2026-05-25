package com.payroll.controller;

import com.payroll.dto.request.NopayDaysRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.dto.response.NopayDaysResponseDTO;
import com.payroll.formula.PayrollContextBuilder;
import com.payroll.service.NopayDaysService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payroll/nopay-days")
@RequiredArgsConstructor
public class NopayDaysController {

    private final NopayDaysService nopayDaysService;

    // GET /payroll/nopay-days
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<NopayDaysResponseDTO>>> getAllNopayDays(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow,
            @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Nopay days fetched successfully",
                nopayDaysService.getAllNopayDays(showDefaultRow, isActive)));
    }

    // GET /payroll/nopay-days/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<NopayDaysResponseDTO>> getNopayDaysById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Nopay days fetched successfully",
                nopayDaysService.getNopayDaysById(id)));
    }

    // POST /payroll/nopay-days
    @PostMapping
    public ResponseEntity<ApiResponseDTO<NopayDaysResponseDTO>> createNopayDays(
            @Valid @RequestBody NopayDaysRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Nopay days created successfully",
                        nopayDaysService.createNopayDays(requestDTO)));
    }

    // PUT /payroll/nopay-days/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<NopayDaysResponseDTO>> updateNopayDays(
            @PathVariable Long id,
            @Valid @RequestBody NopayDaysRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Nopay days updated successfully",
                nopayDaysService.updateNopayDays(id, requestDTO)));
    }

    // DELETE /payroll/nopay-days/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteNopayDays(@PathVariable Long id) {
        nopayDaysService.deleteNopayDays(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Nopay days deleted successfully", null));
    }

    /**
     * POST /payroll/nopay-days/{id}/calculate
     *
     * Computes the nopay deduction amount using the linked formula (or fixed days fallback).
     *
     * Request body example:
     * {
     *   "basicSalary": 80000,
     *   "workingDays": 26,
     *   "nopayDays": 2,
     *   "customVariables": {}
     * }
     */
    @PostMapping("/{id}/calculate")
    public ResponseEntity<ApiResponseDTO<FormulaEvaluateResponseDTO>> calculateNopayAmount(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        Map<String, Object> context = PayrollContextBuilder.builder()
                .basicSalary(parseBigDecimal(body.get("basicSalary")))
                .workingDays(parseInteger(body.get("workingDays"), 26))
                .nopayDays(parseInteger(body.get("nopayDays"), 0))
                .customVariables(parseCustomVariables(body.get("customVariables")))
                .build();

        return ResponseEntity.ok(ApiResponseDTO.success(
                "Nopay amount calculated successfully",
                nopayDaysService.calculateAmount(id, context)));
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
