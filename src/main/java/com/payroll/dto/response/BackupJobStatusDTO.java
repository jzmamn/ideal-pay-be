package com.payroll.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackupJobStatusDTO {

    private String jobId;

    /** BACKUP or RESTORE */
    private String type;

    /** RUNNING, COMPLETED or FAILED */
    private String state;

    /** 0–100 */
    private int progress;

    /** Table currently being dumped / statement phase during restore */
    private String currentStep;

    private int tablesDone;
    private int totalTables;

    /** Absolute path of the backup file produced / consumed */
    private String filePath;

    private String error;

    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
