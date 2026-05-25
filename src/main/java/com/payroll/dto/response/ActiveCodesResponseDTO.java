package com.payroll.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiveCodesResponseDTO {

    private List<CodeDropdownDTO> fixedAllowances;
    private List<CodeDropdownDTO> fixedDeductions;
    private List<CodeDropdownDTO> nopayDays;
    private List<CodeDropdownDTO> overtimes;
    private List<CodeDropdownDTO> variableAllowances;
    private List<CodeDropdownDTO> variableDeductions;
}
