package com.payroll.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUrolResponseDTO {

    private Long id;

    private Long userId;
    private String userCode;
    private String userName;

    private Long urolId;
    private String urolCode;
    private String urolName;

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
