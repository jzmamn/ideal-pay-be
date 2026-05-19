package com.payroll.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyResponseDTO {

    private Long id;
    private String code;
    private String name;
    private String contactPerson;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String addressEmail;
    private String telephone;
    private String fax;
    private String email;
    private String logo;
    private Boolean isActive;
    private Long createdBy;
    private LocalDateTime createdDate;
    private Long modifiedBy;
    private LocalDateTime modifiedDate;
}
