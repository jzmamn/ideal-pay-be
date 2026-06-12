package com.payroll.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Summary returned after a payroll component load operation.
 * Includes counts and any per-employee errors (load continues even if one employee fails).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadSummaryDTO {

    /** Number of active employees processed during the load. */
    private int employeesProcessed;

    /** Total number of emp_* records created or updated. */
    private int recordsUpserted;

    /**
     * Non-fatal errors encountered while processing individual employees.
     * Format: "EMP-{no}: {message}"
     */
    @Builder.Default
    private List<String> errors = new ArrayList<>();
}
