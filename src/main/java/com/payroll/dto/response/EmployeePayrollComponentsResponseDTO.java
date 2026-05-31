package com.payroll.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeePayrollComponentsResponseDTO {

    private EmployeeResponseDTO employee;
    private List<EmployeeFixedAllowanceResponseDTO> fixedAllowances;
    private List<EmployeeFixedDeductionResponseDTO> fixedDeductions;
    private List<EmployeeVariableAllowanceResponseDTO> variableAllowances;
    private List<EmployeeVariableDeductionResponseDTO> variableDeductions;
    private List<EmployeeNopayResponseDTO> nopays;
    private List<EmployeeOvertimeResponseDTO> overtimes;
}
