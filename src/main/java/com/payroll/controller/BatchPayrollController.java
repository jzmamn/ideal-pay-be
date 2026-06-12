package com.payroll.controller;

import com.payroll.dto.request.BatchSaveRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.LoadSummaryDTO;
import com.payroll.service.BatchPayrollService;
import com.payroll.service.PayrollComponentLoadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payroll/batch-allowance")
@RequiredArgsConstructor
public class BatchPayrollController {

    private final BatchPayrollService batchPayrollService;
    private final PayrollComponentLoadService payrollComponentLoadService;

    /**
     * Load pivot data for all component types for the given month/year.
     *
     * Response keys:
     *   fixedAllowances, fixedDeductions, variableAllowances,
     *   variableDeductions, overtimes, nopays
     *
     * Each value is the raw SP result — a list of employee rows with
     * dynamic component columns (one column per active component code).
     *
     * GET /payroll/batch-allowance?month=5&year=2026
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<Map<String, List<Map<String, Object>>>>> load(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Batch payroll data fetched successfully",
                batchPayrollService.load(month, year)));
    }

    /**
     * Load (or reload) all payroll components for ALL active employees.
     *
     * <p>Working days are resolved automatically from the {@code PayrollPeriod} record
     * for the given month/year — configure them when creating the payroll period.
     *
     * POST /payroll/batch-allowance/load?month=5&year=2026&userId=1
     */
    @PostMapping("/load")
    public ResponseEntity<ApiResponseDTO<LoadSummaryDTO>> loadComponents(
            @RequestParam Integer month,
            @RequestParam Integer year,
            @RequestParam Long userId) {
        LoadSummaryDTO summary = payrollComponentLoadService.loadForPeriod(month, year, userId);
        return ResponseEntity.ok(ApiResponseDTO.success("Payroll components loaded successfully", summary));
    }

    /**
     * Load (or reload) all payroll components for a SINGLE employee.
     *
     * POST /payroll/batch-allowance/load/employee?empId=5&month=5&year=2026&userId=1
     */
    @PostMapping("/load/employee")
    public ResponseEntity<ApiResponseDTO<LoadSummaryDTO>> loadComponentsForEmployee(
            @RequestParam Long empId,
            @RequestParam Integer month,
            @RequestParam Integer year,
            @RequestParam Long userId) {
        LoadSummaryDTO summary = payrollComponentLoadService.loadForEmployee(empId, month, year, userId);
        return ResponseEntity.ok(ApiResponseDTO.success("Payroll components loaded for employee", summary));
    }

    /**
     * Save batch entries — upserts into component tables.
     * Zero/null amounts are silently skipped.
     * Returns 400 if any employee's month is already LOCKED.
     *
     * POST /payroll/batch-allowance?modifiedBy=1
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<Void>> save(
            @Valid @RequestBody BatchSaveRequestDTO requestDTO,
            @RequestParam Long modifiedBy) {
        batchPayrollService.save(requestDTO, modifiedBy);
        return ResponseEntity.ok(ApiResponseDTO.success("Batch payroll saved successfully", null));
    }
}
