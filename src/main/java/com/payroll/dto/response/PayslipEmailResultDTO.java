package com.payroll.dto.response;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PayslipEmailResultDTO {
    private int          sent;
    private int          failed;
    private List<String> errors;
}
