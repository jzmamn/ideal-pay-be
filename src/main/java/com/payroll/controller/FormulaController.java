package com.payroll.controller;

import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.FormulaFieldDTO;
import com.payroll.enums.FormulaType;
import com.payroll.service.FixedAllowanceService;
import com.payroll.service.FixedDeductionService;
import com.payroll.service.FormulaFieldService;
import com.payroll.service.OvertimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/formula")
@RequiredArgsConstructor
public class FormulaController {

    private final OvertimeService overtimeService;
    private final FixedAllowanceService fixedAllowanceService;
    private final FixedDeductionService fixedDeductionService;
    private final FormulaFieldService formulaFieldService;

    /**
     * GET /payroll/formula/fields
     *
     * Returns all variables available for use inside a formula expression:
     * basicSalary (Employee Master), active Fixed Allowances, Fixed Deductions,
     * and Overtime types — all excluding default rows.
     */
    @GetMapping("/fields")
    public ResponseEntity<ApiResponseDTO<List<FormulaFieldDTO>>> getAllFormulaFields() {
        List<FormulaFieldDTO> fields = formulaFieldService.getAllFormulaFields();
        return ResponseEntity.ok(ApiResponseDTO.success("Formula fields fetched successfully", fields));
    }

    /**
     * GET /payroll/formula/type/{formulaType}
     *
     * Returns all formula configurations for the given type.
     * Valid values: OVERTIME, FIXED_ALLOWANCE, FIXED_DEDUCTION
     */
    @GetMapping("/type/{formulaType}")
    public ResponseEntity<ApiResponseDTO<List<?>>> getByFormulaType(
            @PathVariable String formulaType) {

        FormulaType type;
        try {
            type = FormulaType.valueOf(formulaType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid formula type: '" + formulaType
                    + "'. Valid values: OVERTIME, FIXED_ALLOWANCE, FIXED_DEDUCTION");
        }

        List<?> data = switch (type) {
            case OVERTIME        -> overtimeService.getAllOvertimes(false, "all");
            case FIXED_ALLOWANCE -> fixedAllowanceService.getAllFixedAllowances(false, "all");
            case FIXED_DEDUCTION -> fixedDeductionService.getAllFixedDeductions(false, "all");
        };

        return ResponseEntity.ok(ApiResponseDTO.success(
                formulaType + " formula configurations fetched successfully", data));
    }
}
