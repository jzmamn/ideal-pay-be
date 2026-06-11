package com.payroll.dto.response;

import lombok.*;

import java.util.List;

/** Describes the expected file format for one importable entity. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportFormatResponseDTO {

    /** EMP_NOPAY | EMP_OT | EMP_LATE | EMP_SALARY_ADVANCE */
    private String entity;

    /** All logical columns, in template order. */
    private List<String> expectedFields;

    /** Columns that must be present and non-blank on every row. */
    private List<String> requiredFields;

    /** Columns forming the in-file duplicate key. */
    private List<String> keyFields;

    /** Columns that must parse as a decimal number ≥ 0. */
    private List<String> numericFields;

    /** Accepted file types. */
    private List<String> supportedFormats;

    /** Example values, aligned with {@code expectedFields}. */
    private List<String> sampleRow;
}
