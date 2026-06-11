package com.payroll.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportCommitResponseDTO {

    private Long importLogId;

    private int insertedRows;

    /** Error rows present in the file that were skipped at commit. */
    private int skippedRows;
}
