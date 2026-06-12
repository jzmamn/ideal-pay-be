package com.payroll.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackupRequestDTO {

    /** Optional override of the configured backup directory. */
    private String path;
}
