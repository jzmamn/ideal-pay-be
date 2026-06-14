package com.payroll.service;

import com.payroll.entity.Employee;
import com.payroll.entity.EmployeeNopay;
import com.payroll.entity.EmployeeVariableAllowance;
import com.payroll.entity.EmployeeVariableDeduction;
import com.payroll.entity.NopayDays;
import com.payroll.entity.SystemSetup;
import com.payroll.entity.VariableAllowance;
import com.payroll.entity.VariableDeduction;
import com.payroll.enums.ComponentType;
import com.payroll.repository.SystemSetupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalaryCalculationEngineServiceTest {

    @Mock
    private SystemSetupRepository systemSetupRepository;

    private SalaryCalculationEngineService service;

    @BeforeEach
    void setUp() {
        service = new SalaryCalculationEngineService(systemSetupRepository);
        when(systemSetupRepository.findByCodeIn(anyCollection())).thenReturn(List.of(
                setup("EMPLOYEE_EPF_PERCENT", "8.00"),
                setup("EMPLOYER_EPF_PERCENT", "12.00"),
                setup("EMPLOYER_ETF_PERCENT", "3.00"),
                setup("EPF_ENABLED", "Y"),
                setup("ETF_ENABLED", "Y"),
                setup("PAYE_ENABLED", "Y")
        ));
    }

    @Test
    void variableAmountsRemainExactAndLiabilityFlagsAffectBasesDuringPayroll() {
        Employee employee = Employee.builder()
                .id(1L)
                .basicSalary(new BigDecimal("100000.00"))
                .build();

        VariableAllowance allowanceType = VariableAllowance.builder()
                .id(11L)
                .code("VA_11")
                .name("Monthly Allowance")
                .isTaxable(true)
                .liableForEpf(true)
                .liableForEtf(true)
                .liableForPaye(true)
                .liableNoPay(true)
                .build();
        EmployeeVariableAllowance allowance = EmployeeVariableAllowance.builder()
                .variableAllowance(allowanceType)
                .amount(new BigDecimal("10000.00"))
                .build();

        VariableDeduction deductionType = VariableDeduction.builder()
                .id(12L)
                .code("VD_12")
                .name("Monthly Deduction")
                .isTaxable(true)
                .liableForEpf(true)
                .liableForEtf(true)
                .liableForPaye(true)
                .liableNoPay(true)
                .build();
        EmployeeVariableDeduction deduction = EmployeeVariableDeduction.builder()
                .variableDeduction(deductionType)
                .amount(new BigDecimal("5000.00"))
                .build();

        EmployeeNopay nopay = EmployeeNopay.builder()
                .nopayDays(NopayDays.builder().id(13L).code("NP_13").name("No Pay").build())
                .days(new BigDecimal("13.00"))
                .amount(new BigDecimal("1000.00"))
                .build();

        SalaryCalculationResult result = service.calculate(
                employee, 26, List.of(), List.of(allowance), List.of(), List.of(),
                List.of(deduction), List.of(nopay), List.of(), List.of());

        assertThat(lineAmount(result, ComponentType.VA)).isEqualByComparingTo("10000.00");
        assertThat(lineAmount(result, ComponentType.VD)).isEqualByComparingTo("5000.00");
        assertThat(result.getTotalAllowances()).isEqualByComparingTo("10000.00");
        assertThat(result.getEpfLiableBase()).isEqualByComparingTo("105000.00");
        assertThat(result.getTaxableEarnings()).isEqualByComparingTo("105000.00");
        assertThat(result.getEmployeeEpf()).isEqualByComparingTo("8400.00");
        assertThat(result.getEtf()).isEqualByComparingTo("3150.00");
    }

    private BigDecimal lineAmount(SalaryCalculationResult result, ComponentType type) {
        return result.getLines().stream()
                .filter(line -> line.getComponentType() == type)
                .findFirst()
                .orElseThrow()
                .getAmount();
    }

    private SystemSetup setup(String code, String value) {
        return SystemSetup.builder().code(code).value(value).build();
    }
}
