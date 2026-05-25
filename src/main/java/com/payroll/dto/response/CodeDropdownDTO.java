package com.payroll.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeDropdownDTO {

    private Long id;
    private String code;
    private String name;
}
