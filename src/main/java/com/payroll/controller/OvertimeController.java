package com.payroll.controller;

import com.payroll.dto.request.OvertimeRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.OvertimeResponseDTO;
import com.payroll.service.OvertimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/overtime")
@RequiredArgsConstructor
public class OvertimeController {

    private final OvertimeService overtimeService;

    // GET /payroll/overtime
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<OvertimeResponseDTO>>> getAllOvertimes(@RequestParam(value = "showDefaultRow", defaultValue = "false") boolean showDefaultRow, @RequestParam(value = "isActive", defaultValue = "all") String isActive) {
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
}
