package com.payroll.service.impl;

import com.payroll.dto.request.PayrollPeriodRequestDTO;
import com.payroll.dto.response.PayrollPeriodResponseDTO;
import com.payroll.entity.PayrollPeriod;
import com.payroll.entity.Usr;
import com.payroll.enums.PayrollStatus;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.PayrollPeriodMapper;
import com.payroll.repository.CompanyRepository;
import com.payroll.repository.EmpPayrollRunRepository;
import com.payroll.repository.PayrollPeriodRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.PayrollPeriodService;
import com.payroll.service.SystemSetupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PayrollPeriodServiceImpl implements PayrollPeriodService {

    private final PayrollPeriodRepository periodRepo;
    private final EmpPayrollRunRepository runRepo;
    private final CompanyRepository companyRepo;
    private final UsrRepository usrRepo;
    private final PayrollPeriodMapper mapper;
    private final SystemSetupService systemSetupService;

    private static final Sort PERIOD_DESC =
            Sort.by(Sort.Direction.DESC, "periodYear", "periodMonth");

    // ── Queries ───────────────────────────────────────────────────────────────

    @Override
    public List<PayrollPeriodResponseDTO> getAllPeriods(Long companyId, Integer year,
                                                        Integer month, PayrollStatus status) {
        List<PayrollPeriod> periods = (companyId != null)
                ? periodRepo.findAllByCompany_Id(companyId, PERIOD_DESC)
                : periodRepo.findAll(PERIOD_DESC);

        return periods.stream()
                .filter(p -> year == null || p.getPeriodYear().equals(year))
                .filter(p -> month == null || p.getPeriodMonth().equals(month))
                .filter(p -> status == null || p.getPayrollStatus() == status)
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Override
    public PayrollPeriodResponseDTO getPeriod(Long id) {
        return mapper.toResponseDTO(findPeriod(id));
    }

    @Override
    public PayrollPeriodResponseDTO getActivePeriod(Long companyId) {
        return periodRepo.findByCompany_IdAndActive(companyId, true)
                .map(mapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active PayrollPeriod", "companyId", companyId));
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PayrollPeriodResponseDTO createPeriod(PayrollPeriodRequestDTO request, Long userId) {
        if (periodRepo.existsByCompany_IdAndPeriodYearAndPeriodMonth(
                request.getCompanyId(), request.getPeriodYear(), request.getPeriodMonth())) {
            throw new IllegalStateException(String.format(
                    "A payroll period already exists for company %d, period %s.",
                    request.getCompanyId(),
                    PayrollPeriod.buildPeriodCode(request.getPeriodYear(), request.getPeriodMonth())));
        }

        Usr user = resolveUser(userId);
        PayrollPeriod period = mapper.toEntity(request);
        period.setCompany(companyRepo.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company", "id", request.getCompanyId())));
        period.setPeriodCode(PayrollPeriod.buildPeriodCode(
                request.getPeriodYear(), request.getPeriodMonth()));
        if (period.getWorkingDays() == null) {
            period.setWorkingDays(systemSetupService.getWorkingDays());
        }
        if (period.getPayrollStatus() == null) period.setPayrollStatus(PayrollStatus.FUTURE);
        if (period.getLocked() == null) period.setLocked(false);
        if (period.getActive() == null) period.setActive(false);
        period.setCreatedBy(user);
        period.setModifiedBy(user);

        return mapper.toResponseDTO(periodRepo.save(period));
    }

    @Override
    @Transactional
    public PayrollPeriodResponseDTO updatePeriod(Long id, PayrollPeriodRequestDTO request, Long userId) {
        PayrollPeriod period = findPeriod(id);

        if (period.getPayrollStatus() == PayrollStatus.CLOSED) {
            throw new IllegalStateException(
                    "Period " + period.getPeriodCode() + " is CLOSED and cannot be edited. Reopen it first.");
        }
        if (Boolean.TRUE.equals(period.getLocked())) {
            throw new IllegalStateException(
                    "Period " + period.getPeriodCode() + " is locked and cannot be edited.");
        }
        if (!period.getCompany().getId().equals(request.getCompanyId())) {
            throw new IllegalArgumentException("A payroll period cannot be moved to another company.");
        }
        if (periodRepo.existsByCompany_IdAndPeriodYearAndPeriodMonthAndIdNot(
                request.getCompanyId(), request.getPeriodYear(), request.getPeriodMonth(), id)) {
            throw new IllegalStateException(String.format(
                    "Another payroll period already exists for company %d, period %s.",
                    request.getCompanyId(),
                    PayrollPeriod.buildPeriodCode(request.getPeriodYear(), request.getPeriodMonth())));
        }

        mapper.updateEntityFromDTO(request, period);
        period.setPeriodCode(PayrollPeriod.buildPeriodCode(
                period.getPeriodYear(), period.getPeriodMonth()));
        period.setModifiedBy(resolveUser(userId));

        return mapper.toResponseDTO(periodRepo.save(period));
    }

    @Override
    @Transactional
    public void deletePeriod(Long id) {
        PayrollPeriod period = findPeriod(id);

        if (runRepo.existsByPayrollMonth(period.getPeriodCode())) {
            throw new IllegalStateException(
                    "Period " + period.getPeriodCode()
                    + " has payroll transactions and cannot be deleted.");
        }
        if (Boolean.TRUE.equals(period.getActive())) {
            throw new IllegalStateException(
                    "Period " + period.getPeriodCode()
                    + " is the active period and cannot be deleted. Activate another period first.");
        }
        if (Boolean.TRUE.equals(period.getLocked())
                || period.getPayrollStatus() == PayrollStatus.CLOSED) {
            throw new IllegalStateException(
                    "Period " + period.getPeriodCode() + " is locked/closed and cannot be deleted.");
        }

        periodRepo.delete(period);
    }

    // ── Status transitions ────────────────────────────────────────────────────

    @Override
    @Transactional
    public PayrollPeriodResponseDTO activatePeriod(Long id, Long userId) {
        PayrollPeriod period = findPeriod(id);
        if (period.getPayrollStatus() == PayrollStatus.CLOSED) {
            throw new IllegalStateException(
                    "A CLOSED period cannot be activated. Reopen it first.");
        }

        Usr user = resolveUser(userId);

        // Deactivate the previously active period(s) for this company
        periodRepo.findAllByCompany_IdAndActive(period.getCompany().getId(), true)
                .stream()
                .filter(p -> !p.getId().equals(id))
                .forEach(p -> {
                    p.setActive(false);
                    p.setModifiedBy(user);
                    periodRepo.save(p);
                });

        period.setActive(true);
        period.setModifiedBy(user);
        return mapper.toResponseDTO(periodRepo.save(period));
    }

    @Override
    @Transactional
    public PayrollPeriodResponseDTO openPeriod(Long id, Long userId) {
        PayrollPeriod period = findPeriod(id);
        requireStatus(period, PayrollStatus.FUTURE, PayrollStatus.REOPENED);
        period.setPayrollStatus(PayrollStatus.OPEN);
        period.setLocked(false);
        period.setModifiedBy(resolveUser(userId));
        return mapper.toResponseDTO(periodRepo.save(period));
    }

    @Override
    @Transactional
    public PayrollPeriodResponseDTO startProcessing(Long id, Long userId) {
        PayrollPeriod period = findPeriod(id);
        requireStatus(period, PayrollStatus.OPEN, PayrollStatus.REOPENED);
        period.setPayrollStatus(PayrollStatus.PROCESSING);
        period.setLocked(true); // locks salary, attendance, OT, allowance, deduction changes
        period.setModifiedBy(resolveUser(userId));
        return mapper.toResponseDTO(periodRepo.save(period));
    }

    @Override
    @Transactional
    public PayrollPeriodResponseDTO completePeriod(Long id, Long userId) {
        PayrollPeriod period = findPeriod(id);
        requireStatus(period, PayrollStatus.PROCESSING);
        period.setPayrollStatus(PayrollStatus.COMPLETED);
        period.setLocked(true);
        period.setPayrollRunDate(LocalDate.now());
        period.setModifiedBy(resolveUser(userId));
        return mapper.toResponseDTO(periodRepo.save(period));
    }

    @Override
    @Transactional
    public PayrollPeriodResponseDTO closePeriod(Long id, Long userId) {
        PayrollPeriod period = findPeriod(id);
        requireStatus(period, PayrollStatus.COMPLETED, PayrollStatus.OPEN, PayrollStatus.REOPENED);

        Usr user = resolveUser(userId);
        period.setPayrollStatus(PayrollStatus.CLOSED);
        period.setLocked(true);
        period.setActive(false); // a closed period can no longer be the active one
        period.setClosedDate(LocalDateTime.now());
        period.setClosedBy(user);
        period.setModifiedBy(user);
        return mapper.toResponseDTO(periodRepo.save(period));
    }

    @Override
    @Transactional
    public PayrollPeriodResponseDTO reopenPeriod(Long id, Long userId) {
        PayrollPeriod period = findPeriod(id);
        requireStatus(period, PayrollStatus.CLOSED);
        period.setPayrollStatus(PayrollStatus.REOPENED);
        period.setLocked(false);
        period.setClosedDate(null);
        period.setClosedBy(null);
        period.setModifiedBy(resolveUser(userId));
        return mapper.toResponseDTO(periodRepo.save(period));
    }

    // ── Legacy compatibility ──────────────────────────────────────────────────

    @Override
    public boolean isPeriodOpen(String month) {
        List<PayrollPeriod> periods = periodRepo.findAllByPeriodCode(month);
        if (periods.isEmpty()) return true; // period not yet created → treated as open
        return periods.stream().allMatch(this::allowsEntry);
    }

    @Override
    public boolean isPeriodOpen(Long companyId, String month) {
        return periodRepo.findByCompany_IdAndPeriodCode(companyId, month)
                .map(this::allowsEntry)
                .orElse(true);
    }

    private boolean allowsEntry(PayrollPeriod p) {
        return p.getPayrollStatus().allowsDataEntry() && !Boolean.TRUE.equals(p.getLocked());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private PayrollPeriod findPeriod(Long id) {
        return periodRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollPeriod", "id", id));
    }

    private void requireStatus(PayrollPeriod period, PayrollStatus... allowed) {
        for (PayrollStatus s : allowed) {
            if (period.getPayrollStatus() == s) return;
        }
        throw new IllegalStateException(String.format(
                "Period %s is %s — this action requires status %s.",
                period.getPeriodCode(), period.getPayrollStatus(), List.of(allowed)));
    }

    /**
     * Resolves the audit user: prefers the Spring Security logged-in username,
     * falls back to the explicit userId parameter (legacy callers).
     */
    private Usr resolveUser(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null
                && !"anonymousUser".equals(auth.getName())) {
            return usrRepo.findByUserName(auth.getName())
                    .orElseGet(() -> referenceOrFail(userId));
        }
        return referenceOrFail(userId);
    }

    private Usr referenceOrFail(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "No authenticated user and no userId provided for audit fields.");
        }
        return usrRepo.getReferenceById(userId);
    }
}
