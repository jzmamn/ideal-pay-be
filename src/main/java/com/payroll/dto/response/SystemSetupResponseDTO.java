package com.payroll.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemSetupResponseDTO {
    private Long id;
    private String code;
    private String name;
    private String value;
    private String description;
    private Boolean isActive;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Long createdById;
    private LocalDateTime createdDate;
    private Long modifiedById;
    private LocalDateTime modifiedDate;
}
