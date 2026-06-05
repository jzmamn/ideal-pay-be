package com.payroll.dto.request;

import com.payroll.entity.EmailTemplate.TemplateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EmailTemplateRequestDTO {

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotNull
    private TemplateType templateType;

    @NotBlank
    @Size(max = 500)
    private String subject;

    @NotBlank
    private String body;

    @NotNull
    private Boolean isActive;

    /** Optional — ID of the SMTP config to use when sending this template. */
    private Long emailConfigId;
}
