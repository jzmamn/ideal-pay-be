package com.payroll.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BankTransferTemplateRequestDTO {

    @NotNull(message = "Bank is required")
    private Long bankId;

    @NotBlank(message = "Bank code is required")
    @Size(max = 20)
    private String bankCode;

    @NotBlank(message = "Bank name is required")
    @Size(max = 100)
    private String bankName;

    @Size(max = 5000)
    private String headerTemplate;

    @NotBlank(message = "Detail template is required")
    @Size(max = 5000)
    private String detailTemplate;

    @Size(max = 5000)
    private String footerTemplate;

    @NotBlank(message = "File extension is required")
    @Size(max = 10)
    private String fileExtension;

    @NotNull
    private Long modifiedBy;
}
