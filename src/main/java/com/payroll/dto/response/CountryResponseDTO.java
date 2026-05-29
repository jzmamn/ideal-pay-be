package com.payroll.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryResponseDTO {

    private Long id;
    private String code;
    private String name;
    private Boolean isActive;
    private String iso2;
    private String iso3;
    private Long phoneCode;
}
