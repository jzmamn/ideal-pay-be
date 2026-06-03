package com.payroll.service.impl;

import com.payroll.dto.response.PayrollPeriodResponseDTO;
import com.payroll.entity.PayrollPeriod;
import com.payroll.enums.PayrollRunStatus;
import com.payroll.enums.PeriodStatus;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.repository.EmpPayrollRunRepository;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.PayrollPeriodRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.PayrollPeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PayrollPeriodServiceImpl implements PayrollPeriodService {

    private final PayrollPeriodRepository periodRepo;
    private final EmpPayrollRunRepository runRepo;
    private final EmployeeRepository employeeRepo;
    private final UsrRepository usrRepo;

    private static final Sort MONTH_DESC = Sort.by("periodMonth").descending();

    // ── Open ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PayrollPeriodResponseDTO openPeriod(String month, Long userId) {
        if (periodRepo.existsByPeriodMonth(month)) {
            throw new IllegalStateException("A payroll period already exists for month: " + month);
        }
        PayrollPeriod period = PayrollPeriod.builder()
                .periodMonth(month)
                .status(PeriodStatus.OPEN)
                .createdBy(usrRepo.getReferenceById(userId))
                .modifiedBy(usrRepo.getReferenceById(userId))
                .build();
        return toDTO(periodRepo.save(period));
    }

    // ── Close ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PayrollPeriodResponseDTO closePeriod(String month, Long userId) {
        PayrollPeriod period = findPeriod(month);
        if (period.getStatus() == PeriodStatus.CLOSED) {
            throw new IllegalStateException("Period " + month + " is already closed.");
        }

        // Verify every active employee has a LOCKED run for this month
        long activeEmpCount  = employeeRepo.countByIsActive(true);
        long lockedRunCount  = runRepo.findAllByPayrollMonthAndStatus(month, PayrollRunStatus.LOCKED,
                                       Sort.unsorted()).size();

        if (lockedRunCount < activeEmpCount) {
            throw new IllegalStateException(String.format(
                "Cannot close period %s — %d of %d active employees are not yet locked.",
                month, activeEmpCount - lockedRunCount, activeEmpCount));
        }

        return doClose(period, userId);
    }

    @Override
    @Transactional
    public PayrollPeriodResponseDTO forceClosePeriod(String month, Long userId) {
        PayrollPeriod period = findPeriod(month);
        if (period.getStatus() == PeriodStatus.CLOSED) {
            throw new IllegalStateException("Period " + month + " is already closed.");
        }
        return doClose(period, userId);
    }

    private PayrollPeriodResponseDTO doClose(PayrollPeriod period, Long userId) {
        period.setStatus(PeriodStatus.CLOSED);
        period.setClosedDate(LocalDateTime.now());
        period.setClosedBy(usrRepo.getReferenceById(userId));
        period.setModifiedBy(usrRepo.getReferenceById(userId));
        return toDTO(periodRepo.save(period));
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Override
    public boolean isPeriodOpen(String month) {
        return periodRepo.findByPeriodMonth(month)
                .map(p -> p.getStatus() == PeriodStatus.OPEN)
                .orElse(true); // period not yet created → treated as open
    }

    @Override
    public List<PayrollPeriodResponseDTO> getAllPeriods() {
        return periodRepo.findAll(MONTH_DESC).stream().map(this::toDTO).toList();
    }

    @Override
    public List<PayrollPeriodResponseDTO> getOpenPeriods() {
        return periodRepo.findAllByStatus(PeriodStatus.OPEN, MONTH_DESC).stream().map(this::toDTO).toList();
    }

    @Override
    public PayrollPeriodResponseDTO getPeriod(String month) {
        return toDTO(findPeriod(month));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private PayrollPeriod findPeriod(String month) {
        return periodRepo.findByPeriodMonth(month)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollPeriod", "month", month));
    }

    private PayrollPeriodResponseDTO toDTO(PayrollPeriod p) {
        return PayrollPeriodResponseDTO.builder()
                .id(p.getId())
                .periodMonth(p.getPeriodMonth())
                .status(p.getStatus())
                .closedDate(p.getClosedDate())
                .closedByUserName(p.getClosedBy() != null ? p.getClosedBy().getUserName() : null)
                .createdById(p.getCreatedBy() != null ? p.getCreatedBy().getId() : null)
                .createdByUserName(p.getCreatedBy() != null ? p.getCreatedBy().getUserName() : null)
                .createdDate(p.getCreatedDate())
                .modifiedById(p.getModifiedBy() != null ? p.getModifiedBy().getId() : null)
                .modifiedByUserName(p.getModifiedBy() != null ? p.getModifiedBy().getUserName() : null)
                .modifiedDate(p.getModifiedDate())
                .build();
    }
}
