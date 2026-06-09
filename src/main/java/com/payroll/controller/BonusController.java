package com.payroll.controller;

import com.payroll.dto.request.BonusRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.BonusResponseDTO;
import com.payroll.dto.response.FormulaEvaluateResponseDTO;
import com.payroll.service.BonusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payroll/bonus")
@RequiredArgsConstructor
public class BonusController {

    private final BonusService bonusService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<BonusResponseDTO>>> getAllBonuses(
            @RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow,
            @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Bonuses fetched successfully",
                bonusService.getAllBonuses(showDefaultRow, isActive)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<BonusResponseDTO>> getBonusById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Bonus fetched successfully",
                bonusService.getBonusById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<BonusResponseDTO>> createBonus(
            @Valid @RequestBody BonusRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Bonus created successfully",
                        bonusService.createBonus(requestDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<BonusResponseDTO>> updateBonus(
            @PathVariable Long id,
            @Valid @RequestBody BonusRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Bonus updated successfully",
                bonusService.updateBonus(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteBonus(@PathVariable Long id) {
        bonusService.deleteBonus(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Bonus deleted successfully", null));
    }

    @PostMapping("/{id}/calculate")
    public ResponseEntity<ApiResponseDTO<FormulaEvaluateResponseDTO>> calculateAmount(
            @PathVariable Long id,
            @RequestBody Map<String, Object> context) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Bonus amount calculated successfully",
                bonusService.calculateAmount(id, context)));
    }
}
