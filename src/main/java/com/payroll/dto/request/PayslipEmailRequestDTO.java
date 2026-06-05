package com.payroll.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PayslipEmailRequestDTO {

    @NotEmpty
    private List<Long> runIds;

    /** "portrait" or "landscape" */
    @NotNull
    private String layout;

    /** Optional email template ID. If null, falls back to built-in HTML. */
    private Long templateId;

    /** Optional payslip PDF template ID for the attachment. Falls back to active PDF template. */
    private Long pdfTemplateId;
}
