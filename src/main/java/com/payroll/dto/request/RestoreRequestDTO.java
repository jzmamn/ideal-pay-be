package com.payroll.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestoreRequestDTO {

    /** Optional override of the configured backup directory. */
    private String path;

    /** Name of the .sql backup file inside the backup directory. */
    @NotBlank(message = "fileName is required")
    private String fileName;
}
