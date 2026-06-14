package com.payroll.dto.request;

import com.payroll.entity.VariableAllowance;
import com.payroll.entity.VariableDeduction;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class VariableComponentConfigurationTest {

    @Test
    void variableComponentTypesDoNotExposeAmountOrFormulaConfiguration() {
        assertNoCalculatedAmountFields(VariableAllowance.class);
        assertNoCalculatedAmountFields(VariableDeduction.class);
        assertNoCalculatedAmountFields(VariableAllowanceRequestDTO.class);
        assertNoCalculatedAmountFields(VariableDeductionRequestDTO.class);
    }

    private void assertNoCalculatedAmountFields(Class<?> type) {
        Set<String> fields = Stream.of(type.getDeclaredFields())
                .map(java.lang.reflect.Field::getName)
                .collect(Collectors.toSet());

        assertThat(fields).doesNotContain("amount", "defaultAmount", "formula", "formulaEnabled");
    }
}
