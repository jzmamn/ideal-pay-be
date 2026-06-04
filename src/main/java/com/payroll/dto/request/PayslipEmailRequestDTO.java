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
}
