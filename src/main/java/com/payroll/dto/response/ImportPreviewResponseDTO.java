package com.payroll.dto.response;

import lombok.*;

import java.util.List;
import java.util.Map;

/** Response of upload / re-validate: staged session plus full row preview. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportPreviewResponseDTO {

    private String sessionId;

    private String entity;

    private String payrollMonth;

    private String fileName;

    /** Column headers detected in the uploaded file, in file order. */
    private List<String> detectedHeaders;

    /** Logical fields the entity expects, in template order. */
    private List<String> expectedFields;

    /** Current mapping: expected field → file header (absent = unmapped). */
    private Map<String, String> mapping;

    private int totalRows;

    private int validRows;

    private int errorRows;

    private List<ImportRowDTO> rows;
}
