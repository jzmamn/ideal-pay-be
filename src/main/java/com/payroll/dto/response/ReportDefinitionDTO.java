package com.payroll.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Full metadata for a report registered in the generic report engine
 * (ADR-001, Option B): identity/labels for the picker UI, the filter
 * parameters it accepts, and the columns its stored procedure returns.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDefinitionDTO {

    private String reportKey;
    private String label;
    private String description;
    private String icon;
    private String category;
    private List<ReportParameterDTO> parameters;
    private List<ReportColumnDTO> columns;
}
