package com.payroll.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackupFileDTO {

    private String fileName;
    private long sizeBytes;
    private LocalDateTime createdAt;
}
