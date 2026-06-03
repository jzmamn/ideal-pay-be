package com.payroll.controller;

import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.PayrollPeriodResponseDTO;
import com.payroll.service.PayrollPeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/period")
@RequiredArgsConstructor
public class PayrollPeriodController {

    private final PayrollPeriodService periodService;

    /** List all periods (newest first) */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<PayrollPeriodResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponseDTO.success("Periods fetched", periodService.getAllPeriods()));
    }

    /** List only OPEN periods */
    @GetMapping("/open")
    public ResponseEntity<ApiResponseDTO<List<PayrollPeriodResponseDTO>>> getOpen() {
        return ResponseEntity.ok(ApiResponseDTO.success("Open periods fetched", periodService.getOpenPeriods()));
    }

    /** Get a specific period by month (YYYY-MM) */
    @GetMapping("/{month}")
    public ResponseEntity<ApiResponseDTO<PayrollPeriodResponseDTO>> getByMonth(@PathVariable String month) {
        return ResponseEntity.ok(ApiResponseDTO.success("Period fetched", periodService.getPeriod(month)));
    }

    /** Open a new payroll period */
    @PostMapping("/open/{month}")
    public ResponseEntity<ApiResponseDTO<PayrollPeriodResponseDTO>> openPeriod(
            @PathVariable String month,
            @RequestParam Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(
                "Period opened", periodService.openPeriod(month, userId)));
    }

    /**
     * Close a period — only succeeds when all active employees have a LOCKED run.
     */
    @PostMapping("/close/{month}")
    public ResponseEntity<ApiResponseDTO<PayrollPeriodResponseDTO>> closePeriod(
            @PathVariable String month,
            @RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Period closed", periodService.closePeriod(month, userId)));
    }

    /**
     * Force-close a period regardless of lock status (admin use only).
     */
    @PostMapping("/force-close/{month}")
    public ResponseEntity<ApiResponseDTO<PayrollPeriodResponseDTO>> forceClosePeriod(
            @PathVariable String month,
            @RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Period force-closed", periodService.forceClosePeriod(month, userId)));
    }
}
