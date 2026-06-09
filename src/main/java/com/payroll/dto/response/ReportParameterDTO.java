package com.payroll.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Metadata describing a single filter/parameter accepted by a report.
 * Drives both the dynamic stored-procedure binding (server) and the
 * generic filter UI (frontend).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportParameterDTO {

    private String paramKey;
    private String label;
    private String paramType;
    private boolean required;
    private String defaultValue;
}
