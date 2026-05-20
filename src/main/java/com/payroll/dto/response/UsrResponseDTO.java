package com.payroll.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsrResponseDTO {

    private Long id;
    private String code;
    private String name;
    private String userName;
    private String email;
    // password intentionally excluded from response
    private Long roleId;
    private String roleName;
    private Boolean isActive;
    private Long createdById;
    private String createdByCode;
    private String createdByUserName;
    private LocalDateTime createdDate;
    private Long modifiedById;
    private String modifiedByCode;
    private String modifiedByUserName;
    private LocalDateTime modifiedDate;
}
