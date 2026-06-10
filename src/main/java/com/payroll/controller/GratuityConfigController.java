package com.payroll.controller;

import com.payroll.dto.request.GratuityConfigRequest;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.dto.response.GratuityConfigResponse;
import com.payroll.service.GratuityConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payroll/gratuity-config")
@RequiredArgsConstructor
public class GratuityConfigController {

    private final GratuityConfigService service;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<GratuityConfigResponse>>> getAll(
            @RequestParam(defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success("Gratuity configs fetched", service.getAll(isActive)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponseDTO<GratuityConfigResponse>> getActive() {
        return ResponseEntity.ok(ApiResponseDTO.success("Active gratuity config fetched", service.getActive()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<GratuityConfigResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("Gratuity config fetched", service.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<GratuityConfigResponse>> create(
            @Valid @RequestBody GratuityConfigRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Gratuity config created", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<GratuityConfigResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody GratuityConfigRequest request) {
        return ResponseEntity.ok(ApiResponseDTO.success("Gratuity config updated", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Gratuity config deleted", null));
    }

    /**
     * POST /payroll/gratuity-config/{id}/calculate
     * Body: { "basicSalary": 120000, "yearsOfService": 5 }
     */
    @PostMapping("/{id}/calculate")
    public ResponseEntity<ApiResponseDTO<FormulaEvaluateResponseDTO>> calculate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        Map<String, Object> context = Map.of(
                "basicSalary",    parseBD(body.get("basicSalary")),
                "yearsOfService", parseBD(body.get("yearsOfService"))
        );
        return ResponseEntity.ok(ApiResponseDTO.success("Gratuity calculated",
                service.calculateAmount(id, context)));
    }

    private BigDecimal parseBD(Object v) {
        if (v == null) return BigDecimal.ZERO;
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }
}
