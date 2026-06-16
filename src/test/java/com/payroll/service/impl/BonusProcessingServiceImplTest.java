package com.payroll.service.impl;

import com.payroll.dto.request.BonusProcessingCalculateRequestDTO;
import com.payroll.dto.response.BonusProcessingBatchResponseDTO;
import com.payroll.entity.Bonus;
import com.payroll.entity.BonusProcessingBatch;
import com.payroll.entity.Employee;
import com.payroll.entity.EmployeeBonus;
import com.payroll.entity.Usr;
import com.payroll.enums.BonusCalculationMethod;
import com.payroll.enums.BonusStatus;
import com.payroll.exception.FormulaEvaluationException;
import com.payroll.repository.BonusProcessingBatchRepository;
import com.payroll.repository.BonusRepository;
import com.payroll.repository.EmployeeBonusRepository;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.FormulaEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BonusProcessingServiceImplTest {

    private BonusProcessingBatchRepository batchRepository;
    private EmployeeBonusRepository employeeBonusRepository;
    private BonusRepository bonusRepository;
    private EmployeeRepository employeeRepository;
    private UsrRepository usrRepository;
    private FormulaEngineService formulaEngineService;
    private BonusProcessingServiceImpl service;

    @BeforeEach
    void setUp() {
        batchRepository = mock(BonusProcessingBatchRepository.class);
        employeeBonusRepository = mock(EmployeeBonusRepository.class);
        bonusRepository = mock(BonusRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        usrRepository = mock(UsrRepository.class);
        formulaEngineService = mock(FormulaEngineService.class);
        service = new BonusProcessingServiceImpl(
                batchRepository,
                employeeBonusRepository,
                bonusRepository,
                employeeRepository,
                usrRepository,
                formulaEngineService);
    }

    @Test
    void calculateUsesConfiguredFormulaLiteral() {
        // A "fixed" bonus is now expressed as a literal-value formula, e.g. "25000".
        Bonus bonus = bonus("25000");
        Employee employee = employee();
        Usr user = Usr.builder().id(9L).userName("tester").build();
        BonusProcessingCalculateRequestDTO request = request();

        when(bonusRepository.findById(1L)).thenReturn(Optional.of(bonus));
        when(employeeRepository.findAllById(List.of(10L))).thenReturn(List.of(employee));
        when(usrRepository.getReferenceById(9L)).thenReturn(user);
        when(formulaEngineService.evaluate("25000", any())).thenReturn(new BigDecimal("25000.00"));
        when(batchRepository.save(any(BonusProcessingBatch.class))).thenAnswer(invocation -> {
            BonusProcessingBatch batch = invocation.getArgument(0);
            batch.setId(100L);
            return batch;
        });
        when(employeeBonusRepository.save(any(EmployeeBonus.class))).thenAnswer(invocation -> {
            EmployeeBonus entry = invocation.getArgument(0);
            entry.setId(200L);
            return entry;
        });

        BonusProcessingBatchResponseDTO result = service.calculate(request);

        assertEquals(new BigDecimal("25000.00"), result.getTotalAmount());
        assertEquals(new BigDecimal("25000.00"), result.getEntries().get(0).getEffectiveAmount());
    }

    @Test
    void calculateRejectsDuplicateActiveBatch() {
        Bonus bonus = bonus("10");
        when(bonusRepository.findById(1L)).thenReturn(Optional.of(bonus));
        when(batchRepository.existsByBonusIdAndPayrollMonthAndStatusIn(any(), any(), anyList()))
                .thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.calculate(request()));
        verify(employeeBonusRepository, never()).save(any());
    }

    @Test
    void calculateRejectsBonusWithNoFormulaConfigured() {
        Bonus bonus = bonus(null);
        when(bonusRepository.findById(1L)).thenReturn(Optional.of(bonus));
        when(employeeRepository.findAllById(List.of(10L))).thenReturn(List.of(employee()));
        when(usrRepository.getReferenceById(9L)).thenReturn(Usr.builder().id(9L).build());
        when(batchRepository.save(any(BonusProcessingBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(IllegalStateException.class, () -> service.calculate(request()));
        verify(employeeBonusRepository, never()).save(any());
    }

    @Test
    void calculateDoesNotSilentlyConvertFormulaFailureToZero() {
        Bonus bonus = bonus("missingValue * 2");
        when(bonusRepository.findById(1L)).thenReturn(Optional.of(bonus));
        when(employeeRepository.findAllById(List.of(10L))).thenReturn(List.of(employee()));
        when(usrRepository.getReferenceById(9L)).thenReturn(Usr.builder().id(9L).build());
        when(batchRepository.save(any(BonusProcessingBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(formulaEngineService.evaluate(any(), any()))
                .thenThrow(new FormulaEvaluationException("Unknown variable"));

        assertThrows(FormulaEvaluationException.class, () -> service.calculate(request()));
        verify(employeeBonusRepository, never()).save(any());
    }

    private Bonus bonus(String formula) {
        return Bonus.builder()
                .id(1L)
                .code("BS_1")
                .name("Annual Bonus")
                .isActive(true)
                .calculationMethod(BonusCalculationMethod.FORMULA_BASED)
                .formula(formula)
                .build();
    }

    private Employee employee() {
        return Employee.builder()
                .id(10L)
                .employeeNo("E001")
                .payrollName("Employee One")
                .basicSalary(new BigDecimal("100000.00"))
                .joinedDate(LocalDate.of(2020, 1, 1))
                .isActive(true)
                .build();
    }

    private BonusProcessingCalculateRequestDTO request() {
        return BonusProcessingCalculateRequestDTO.builder()
                .bonusId(1L)
                .payrollMonth("2026-06")
                .employeeIds(List.of(10L))
                .createdBy(9L)
                .modifiedBy(9L)
                .build();
    }
}
