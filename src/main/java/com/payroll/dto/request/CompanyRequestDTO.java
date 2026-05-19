package com.payroll.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyRequestDTO {

    @NotBlank(message = "Code is required")
    @Size(max = 10, message = "Code must not exceed 10 characters")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Contact person is required")
    @Size(max = 150, message = "Contact person must not exceed 150 characters")
    private String contactPerson;

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    private String addressLine1;

    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    private String addressLine2;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Email(message = "Address email must be a valid email")
    @Size(max = 150, message = "Address email must not exceed 150 characters")
    private String addressEmail;

    @NotBlank(message = "Telephone is required")
    @Size(max = 20, message = "Telephone must not exceed 20 characters")
    private String telephone;

    @Size(max = 20, message = "Fax must not exceed 20 characters")
    private String fax;

    @Email(message = "Email must be a valid email")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @Size(max = 500, message = "Logo must not exceed 500 characters")
    private String logo;

    @NotNull(message = "createdBy is required")
    private Long createdBy;

    @NotNull(message = "modifiedBy is required")
    private Long modifiedBy;
}
