package com.payroll.controller;

import com.payroll.dto.request.NopayRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.dto.response.NopayResponseDTO;
import com.payroll.service.NopayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payroll/nopay")
@RequiredArgsConstructor
public class NopayController {

    private final NopayService nopayService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<NopayResponseDTO>>> getAllNopay(
            @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Nopay types fetched successfully",
                nopayService.getAllNopay(isActive)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<NopayResponseDTO>> getNopayById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Nopay type fetched successfully",
                nopayService.getNopayById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<NopayResponseDTO>> createNopay(
            @Valid @RequestBody NopayRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Nopay type created successfully",
                        nopayService.createNopay(requestDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<NopayResponseDTO>> updateNopay(
            @PathVariable Long id,
            @Valid @RequestBody NopayRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Nopay type updated successfully",
                nopayService.updateNopay(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteNopay(@PathVariable Long id) {
        nopayService.deleteNopay(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Nopay type deleted successfully", null));
    }

    @PostMapping("/{id}/calculate")
    public ResponseEntity<ApiResponseDTO<FormulaEvaluateResponseDTO>> calculateAmount(
            @PathVariable Long id,
            @RequestBody Map<String, Object> context) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Formula evaluated successfully",
                nopayService.calculateAmount(id, context)));
    }
}
