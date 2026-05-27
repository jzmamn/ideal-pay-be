package com.payroll.controller;

import com.payroll.dto.request.NopayDaysRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.NopayDaysResponseDTO;
import com.payroll.service.NopayDaysService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
