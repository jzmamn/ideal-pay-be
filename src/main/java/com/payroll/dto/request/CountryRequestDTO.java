package com.payroll.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 70, message = "Name must not exceed 70 characters")
    private String name;

    @NotBlank(message = "ISO2 code is required")
    @Size(min = 2, max = 2, message = "ISO2 must be exactly 2 characters")
    private String iso2;

    @NotBlank(message = "ISO3 code is required")
    @Size(min = 3, max = 3, message = "ISO3 must be exactly 3 characters")
    private String iso3;

    @NotNull(message = "Phone code is required")
    private Long phoneCode;

    @NotNull(message = "Postcode required flag is required")
    private Boolean postcodeRequired;

    @NotNull(message = "Is EU flag is required")
    private Boolean isEu;
}
