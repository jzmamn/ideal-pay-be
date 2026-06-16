package com.payroll.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Initiates a new bonus processing batch.
 * The caller selects a bonus type, a payroll month, and a set of employees
 * (either explicit IDs or filter criteria). The service calculates amounts
 * using the bonus formula and persists a BonusProcessingBatch + EmployeeBonus entries.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BonusProcessingCalculateRequestDTO {

    @NotNull(message = "Bonus type is required")
    private Long bonusId;

    @NotBlank(message = "Payroll month is required")
    @Size(max = 20)
    private String payrollMonth;

    /**
     * Explicit list of employee IDs to include.
     * When null or empty the service falls back to filter criteria below.
     */
    private List<Long> employeeIds;

    // ── Optional filter criteria (applied when employeeIds is empty) ──────────

    /** Filter by department ID. */
    private Long departmentId;

    /** Filter by branch ID. */
    private Long branchId;

    /** Filter by designation ID. */
    private Long designationId;

    /** Filter by grade ID. */
    private Long gradeId;

    /** Filter by employee type (job category) ID. */
    private Long employeeTypeId;

    /**
     * Extra MVEL context variables merged with the standard context.
     * Keys may include: grossSalary, attendancePercentage, performanceRating,
     * workedDays, salesAchievement, productivityPercentage, etc.
     */
    private Map<String, Object> formulaContext;

    /** Formula values keyed by employee ID; these override the shared formulaContext. */
    private Map<Long, Map<String, Object>> employeeFormulaContexts;

    private String notes;

    @NotNull(message = "Created by is required")
    private Long createdBy;

    @NotNull(message = "Modified by is required")
    private Long modifiedBy;
}
