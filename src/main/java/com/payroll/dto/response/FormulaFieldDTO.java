package com.payroll.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormulaFieldDTO {

    /** The variable name to use inside a formula expression (e.g. {@code basicSalary}, {@code FA_1}). */
    private String variable;

    /** Human-readable label for UI display. */
    private String label;

    /** Data type of the variable (e.g. BigDecimal, Integer, String). */
    private String dataType;

    /** Category grouping for UI organisation (e.g. Employee, Pay Period, Allowance). */
    private String category;

    /** Example value or description of what the variable holds. */
    private String description;
}
