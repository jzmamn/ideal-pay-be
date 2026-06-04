package com.payroll.dto.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BankTransferTemplateResponseDTO {
    private Long id;
    private Long bankId;
    private String bankCode;
    private String bankName;
    private String headerTemplate;
    private String detailTemplate;
    private String footerTemplate;
    private String fileExtension;
}
