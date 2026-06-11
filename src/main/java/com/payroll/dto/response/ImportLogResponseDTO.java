package com.payroll.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportLogResponseDTO {

    private Long id;

    private String entity;

    private String payrollMonth;

    private String fileName;

    private int totalRows;

    private int validRows;

    private int errorRows;

    /** COMMITTED | ROLLED_BACK | LOCKED */
    private String status;

    private List<RowErrorDTO> errorDetail;

    private String createdBy;

    private LocalDateTime createdAt;
}
