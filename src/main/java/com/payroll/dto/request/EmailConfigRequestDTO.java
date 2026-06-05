package com.payroll.dto.request;

import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EmailConfigRequestDTO {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    private String host;

    @NotNull @Min(1) @Max(65535)
    private Integer port;

    @NotBlank @Email
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String fromName;

    @NotBlank @Email
    private String fromAddress;

    @NotNull
    private Boolean useTls;
}
