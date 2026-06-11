package com.payroll.service.impl;

import com.payroll.dto.request.PayrollPeriodRequestDTO;
import com.payroll.dto.response.PayrollPeriodResponseDTO;
import com.payroll.entity.Company;
import com.payroll.entity.PayrollPeriod;
import com.payroll.entity.Usr;
import com.payroll.enums.PayrollStatus;
import com.payroll.mapper.PayrollPeriodMapper;
import com.payroll.repository.CompanyRepository;
import com.payroll.repository.EmpPayrollRunRepository;
import com.payroll.repository.PayrollPeriodRepository;
import com.payroll.repository.UsrRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollPeriodServiceImplTest {

    @Mock private PayrollPeriodRepository periodRepo;
    @Mock private EmpPayrollRunRepository runRepo;
    @Mock private CompanyRepository companyRepo;
    @Mock private UsrRepository usrRepo;

    @Spy  private PayrollPeriodMapper mapper = Mappers.getMapper(PayrollPeriodMapper.class);

    private PayrollPeriodServiceImpl service;

    private Company company;
    private Usr user;

    @BeforeEach
    void setUp() {
        service = new PayrollPeriodServiceImpl(periodRepo, runRepo, companyRepo, usrRepo, mapper);
        company = Company.builder().id(1L).name("Ideal Pvt Ltd").build();
        user = Usr.builder().id(7L).userName("admin").build();
        lenient().when(usrRepo.getReferenceById(7L)).thenReturn(user);
        lenient().when(periodRepo.save(any(PayrollPeriod.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    private PayrollPeriodRequestDTO request() {
        return PayrollPeriodRequestDTO.builder()
                .companyId(1L)
                .periodYear(2026)
                .periodMonth(6)
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .build();
    }

    private PayrollPeriod period(PayrollStatus status, boolean locked, boolean active) {
        return PayrollPeriod.builder()
                .id(10L)
                .company(company)
                .periodYear(2026)
                .periodMonth(6)
                .periodCode("2026-06")
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .workingDays(26)
                .payrollStatus(status)
                .locked(locked)
                .active(active)
                .createdBy(user)
                .modifiedBy(user)
                .build();
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @Test
    void createPeriod_savesWithGeneratedCodeAndDefaults() {
        when(periodRepo.existsByCompany_IdAndPeriodYearAndPeriodMonth(1L, 2026, 6)).thenReturn(false);
        when(companyRepo.findById(1L)).thenReturn(Optional.of(company));

        PayrollPeriodResponseDTO result = service.createPeriod(request(), 7L);

        assertThat(result.getPeriodCode()).isEqualTo("2026-06");
        assertThat(result.getPayrollStatus()).isEqualTo(PayrollStatus.FUTURE);
        assertThat(result.getLocked()).isFalse();
        assertThat(result.getActive()).isFalse();
        assertThat(result.getWorkingDays()).isEqualTo(26);
        verify(periodRepo).save(any(PayrollPeriod.class));
    }

    @Test
    void createPeriod_rejectsDuplicateCompanyYearMonth() {
        when(periodRepo.existsByCompany_IdAndPeriodYearAndPeriodMonth(1L, 2026, 6)).thenReturn(true);

        assertThatThrownBy(() -> service.createPeriod(request(), 7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists");
        verify(periodRepo, never()).save(any());
    }

    // ── Activate ──────────────────────────────────────────────────────────────

    @Test
    void activatePeriod_deactivatesPreviouslyActivePeriod() {
        PayrollPeriod target = period(PayrollStatus.OPEN, false, false);
        PayrollPeriod previous = period(PayrollStatus.OPEN, false, true);
        previous.setId(9L);

        when(periodRepo.findById(10L)).thenReturn(Optional.of(target));
        when(periodRepo.findAllByCompany_IdAndActive(1L, true)).thenReturn(List.of(previous));

        PayrollPeriodResponseDTO result = service.activatePeriod(10L, 7L);

        assertThat(result.getActive()).isTrue();
        assertThat(previous.getActive()).isFalse();
        verify(periodRepo, times(2)).save(any(PayrollPeriod.class));
    }

    @Test
    void activatePeriod_rejectsClosedPeriod() {
        when(periodRepo.findById(10L))
                .thenReturn(Optional.of(period(PayrollStatus.CLOSED, true, false)));

        assertThatThrownBy(() -> service.activatePeriod(10L, 7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CLOSED");
    }

    // ── Close / Reopen ────────────────────────────────────────────────────────

    @Test
    void closePeriod_setsClosedLockedAndInactive() {
        PayrollPeriod completed = period(PayrollStatus.COMPLETED, true, true);
        when(periodRepo.findById(10L)).thenReturn(Optional.of(completed));

        PayrollPeriodResponseDTO result = service.closePeriod(10L, 7L);

        assertThat(result.getPayrollStatus()).isEqualTo(PayrollStatus.CLOSED);
        assertThat(result.getLocked()).isTrue();
        assertThat(result.getActive()).isFalse();
        assertThat(completed.getClosedDate()).isNotNull();
    }

    @Test
    void closePeriod_rejectsInvalidSourceStatus() {
        when(periodRepo.findById(10L))
                .thenReturn(Optional.of(period(PayrollStatus.PROCESSING, true, false)));

        assertThatThrownBy(() -> service.closePeriod(10L, 7L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reopenPeriod_unlocksAndClearsClosure() {
        PayrollPeriod closed = period(PayrollStatus.CLOSED, true, false);
        when(periodRepo.findById(10L)).thenReturn(Optional.of(closed));

        PayrollPeriodResponseDTO result = service.reopenPeriod(10L, 7L);

        assertThat(result.getPayrollStatus()).isEqualTo(PayrollStatus.REOPENED);
        assertThat(result.getLocked()).isFalse();
        assertThat(closed.getClosedDate()).isNull();
        assertThat(closed.getClosedBy()).isNull();
    }

    @Test
    void reopenPeriod_rejectsNonClosedPeriod() {
        when(periodRepo.findById(10L))
                .thenReturn(Optional.of(period(PayrollStatus.OPEN, false, false)));

        assertThatThrownBy(() -> service.reopenPeriod(10L, 7L))
                .isInstanceOf(IllegalStateException.class);
    }

    // ── Update guards ─────────────────────────────────────────────────────────

    @Test
    void updatePeriod_rejectsClosedPeriod() {
        when(periodRepo.findById(10L))
                .thenReturn(Optional.of(period(PayrollStatus.CLOSED, true, false)));

        assertThatThrownBy(() -> service.updatePeriod(10L, request(), 7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CLOSED");
    }

    @Test
    void updatePeriod_rejectsLockedPeriod() {
        when(periodRepo.findById(10L))
                .thenReturn(Optional.of(period(PayrollStatus.PROCESSING, true, false)));

        assertThatThrownBy(() -> service.updatePeriod(10L, request(), 7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("locked");
    }

    // ── Delete guards ─────────────────────────────────────────────────────────

    @Test
    void deletePeriod_rejectsWhenTransactionsExist() {
        when(periodRepo.findById(10L))
                .thenReturn(Optional.of(period(PayrollStatus.OPEN, false, false)));
        when(runRepo.existsByPayrollMonth("2026-06")).thenReturn(true);

        assertThatThrownBy(() -> service.deletePeriod(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("payroll transactions");
        verify(periodRepo, never()).delete(any());
    }

    @Test
    void deletePeriod_deletesWhenNoTransactions() {
        PayrollPeriod p = period(PayrollStatus.FUTURE, false, false);
        when(periodRepo.findById(10L)).thenReturn(Optional.of(p));
        when(runRepo.existsByPayrollMonth("2026-06")).thenReturn(false);

        service.deletePeriod(10L);

        verify(periodRepo).delete(p);
    }

    // ── Transitions ───────────────────────────────────────────────────────────

    @Test
    void startProcessing_locksPeriod() {
        when(periodRepo.findById(10L))
                .thenReturn(Optional.of(period(PayrollStatus.OPEN, false, true)));

        PayrollPeriodResponseDTO result = service.startProcessing(10L, 7L);

        assertThat(result.getPayrollStatus()).isEqualTo(PayrollStatus.PROCESSING);
        assertThat(result.getLocked()).isTrue();
    }

    @Test
    void completePeriod_setsRunDate() {
        when(periodRepo.findById(10L))
                .thenReturn(Optional.of(period(PayrollStatus.PROCESSING, true, true)));

        PayrollPeriodResponseDTO result = service.completePeriod(10L, 7L);

        assertThat(result.getPayrollStatus()).isEqualTo(PayrollStatus.COMPLETED);
        assertThat(result.getPayrollRunDate()).isEqualTo(LocalDate.now());
    }

    // ── Legacy compatibility ──────────────────────────────────────────────────

    @Test
    void isPeriodOpen_trueWhenNoPeriodExists() {
        when(periodRepo.findAllByPeriodCode("2026-07")).thenReturn(List.of());
        assertThat(service.isPeriodOpen("2026-07")).isTrue();
    }

    @Test
    void isPeriodOpen_falseWhenPeriodLockedOrClosed() {
        when(periodRepo.findAllByPeriodCode("2026-06"))
                .thenReturn(List.of(period(PayrollStatus.CLOSED, true, false)));
        assertThat(service.isPeriodOpen("2026-06")).isFalse();
    }
}
