package com.payroll.dto.request;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeProfileSaveRequestDTO {

    @Builder.Default
    private List<EmployeeFixedAllowanceRequestDTO> fixedAllowances = new ArrayList<>();

    @Builder.Default
    private List<EmployeeFixedDeductionRequestDTO> fixedDeductions = new ArrayList<>();

    @Builder.Default
    private List<EmployeeVariableAllowanceRequestDTO> variableAllowances = new ArrayList<>();

    @Builder.Default
    private List<EmployeeVariableDeductionRequestDTO> variableDeductions = new ArrayList<>();

    @Builder.Default
    private List<EmployeeNopayRequestDTO> nopays = new ArrayList<>();

    @Builder.Default
    private List<EmployeeOvertimeRequestDTO> overtimes = new ArrayList<>();
}
