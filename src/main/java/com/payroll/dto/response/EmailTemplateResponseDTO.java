package com.payroll.dto.response;

import com.payroll.entity.EmailTemplate.TemplateType;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EmailTemplateResponseDTO {
    private Long         id;
    private String       name;
    private TemplateType templateType;
    private String       subject;
    private String       body;
    private Boolean      isActive;
    private Long         emailConfigId;
    private String       emailConfigName;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
