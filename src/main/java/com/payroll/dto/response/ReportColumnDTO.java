package com.payroll.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Metadata describing a single output column of a report.
 * The columnKey must match the corresponding alias in the backing
 * stored procedure's result set (e.g. "empCode"). Drives the generic
 * frontend table, export, and print rendering.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportColumnDTO {

    private String columnKey;
    private String label;
    private String dataType;
    private int displayOrder;
}
