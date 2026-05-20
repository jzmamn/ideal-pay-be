package com.payroll.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryResponseDTO {

    private Long id;
    private String name;
    private String iso2;
    private String iso3;
    private Long phoneCode;
    private Boolean postcodeRequired;
    private Boolean isEu;
}
