package com.payroll.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

/** Re-validate a staged session after the user remaps columns. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportRemapRequestDTO {

    @NotBlank(message = "Session id is required")
    private String sessionId;

    /** Expected field → file header. */
    @NotNull(message = "Column mapping is required")
    private Map<String, String> mapping;
}
