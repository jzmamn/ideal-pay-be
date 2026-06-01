package com.payroll.controller;

import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.service.PayrollPivotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payroll/pivot")
@RequiredArgsConstructor
public class PayrollPivotController {

    private final PayrollPivotService payrollPivotService;

    @GetMapping("/emp-fa-pivot")
    public ResponseEntity<ApiResponseDTO<List<Map<String, Object>>>> getEmployeeFixedAllowancePivot(
            @RequestParam String month) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee fixed allowance pivot fetched successfully",
                payrollPivotService.getEmployeeFixedAllowancePivot(month)));
    }

    @GetMapping("/emp-fd-pivot")
    public ResponseEntity<ApiResponseDTO<List<Map<String, Object>>>> getEmployeeFixedDeductionPivot(
            @RequestParam String month) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee fixed deduction pivot fetched successfully",
                payrollPivotService.getEmployeeFixedDeductionPivot(month)));
    }

    @GetMapping("/emp-np-pivot")
    public ResponseEntity<ApiResponseDTO<List<Map<String, Object>>>> getEmployeeNopayPivot(
            @RequestParam String month) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee nopay pivot fetched successfully",
                payrollPivotService.getEmployeeNopayPivot(month)));
    }

    @GetMapping("/emp-ot-pivot")
    public ResponseEntity<ApiResponseDTO<List<Map<String, Object>>>> getEmployeeOvertimePivot(
            @RequestParam String month) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee overtime pivot fetched successfully",
                payrollPivotService.getEmployeeOvertimePivot(month)));
    }

    @GetMapping("/emp-va-pivot")
    public ResponseEntity<ApiResponseDTO<List<Map<String, Object>>>> getEmployeeVariableAllowancePivot(
            @RequestParam String month) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee variable allowance pivot fetched successfully",
                payrollPivotService.getEmployeeVariableAllowancePivot(month)));
    }

    @GetMapping("/emp-vd-pivot")
    public ResponseEntity<ApiResponseDTO<List<Map<String, Object>>>> getEmployeeVariableDeductionPivot(
            @RequestParam String month) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee variable deduction pivot fetched successfully",
                payrollPivotService.getEmployeeVariableDeductionPivot(month)));
    }

    @GetMapping("/payroll-monthly-summary")
    public ResponseEntity<ApiResponseDTO<List<Map<String, Object>>>> getPayrollMonthlySummary(
            @RequestParam String month) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Payroll monthly summary fetched successfully",
                payrollPivotService.getPayrollMonthlySummary(month)));
    }

    @GetMapping("/payroll-monthly-detail")
    public ResponseEntity<ApiResponseDTO<List<Map<String, Object>>>> getPayrollMonthlyDetail(
            @RequestParam String month) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Payroll monthly detail fetched successfully",
                payrollPivotService.getPayrollMonthlyDetail(month)));
    }

    @GetMapping("/emp-sal-adv-pivot")
    public ResponseEntity<ApiResponseDTO<List<Map<String, Object>>>> getEmployeeSalaryAdvancePivot(
            @RequestParam String month) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Employee salary advance pivot fetched successfully",
                payrollPivotService.getEmployeeSalaryAdvancePivot(month)));
    }

}