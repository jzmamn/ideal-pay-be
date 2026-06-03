package com.payroll.service.impl;

import com.payroll.dto.request.SalaryIncrementDetailRequest;
import com.payroll.dto.request.SalaryIncrementFaRequest;
import com.payroll.dto.request.SalaryIncrementRequest;
import com.payroll.dto.response.SalaryIncrementResponse;
import com.payroll.entity.*;
import com.payroll.enums.IncrementStatus;
import com.payroll.enums.IncrementType;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.SalaryIncrementMapper;
import com.payroll.repository.*;
import com.payroll.service.SalaryIncrementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalaryIncrementServiceImpl implements SalaryIncrementService {

    private final SalaryIncrementRepository       incrementRepo;
    private final SalaryIncrementDetailRepository detailRepo;
    private final SalaryIncrementFaRepository     faRepo;
    private final EmployeeRepository              employeeRepo;
    private final FixedAllowanceRepository        fixedAllowanceRepo;
    private final EmployeeFixedAllowanceRepository empFaRepo;
    private final UsrRepository                   usrRepo;
    private final SalaryIncrementMapper           mapper;

    private static final Sort ID_DESC = Sort.by("id").descending();

    // ── Queries ───────────────────────────────────────────────────────────────

    @Override
    public List<SalaryIncrementResponse> getAll() {
        return incrementRepo.findAll(ID_DESC).stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<SalaryIncrementResponse> getByType(IncrementType type) {
        return incrementRepo.findAllByType(type, ID_DESC).stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<SalaryIncrementResponse> getByStatus(IncrementStatus status) {
        return incrementRepo.findAllByStatus(status, ID_DESC).stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<SalaryIncrementResponse> getByEffectiveMonth(String month) {
        return incrementRepo.findAllByEffectiveMonth(month, ID_DESC).stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<SalaryIncrementResponse> getByEmployee(Long empId) {
        return detailRepo.findAllByEmployeeId(empId, ID_DESC)
                .stream()
                .map(d -> mapper.toResponse(d.getIncrement()))
                .distinct()
                .toList();
    }

    @Override
    public SalaryIncrementResponse getById(Long id) {
        return mapper.toResponse(findIncrement(id));
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    @Override
    public String nextCode(String effectiveMonth) {
        long seq = incrementRepo.countByEffectiveMonth(effectiveMonth) + 1;
        return String.format("SI-%s-%03d", effectiveMonth, seq);
    }

    @Override
    @Transactional
    public SalaryIncrementResponse create(SalaryIncrementRequest req) {
        SalaryIncrement increment = mapper.toEntity(req);
        // Auto-generate code from effective month
        increment.setCode(nextCode(req.getEffectiveMonth()));
        increment.setStatus(IncrementStatus.DRAFT);
        increment.setCreatedBy(usrRepo.getReferenceById(req.getCreatedBy()));
        increment.setModifiedBy(usrRepo.getReferenceById(req.getModifiedBy()));
        SalaryIncrement saved = incrementRepo.save(increment);
        if (req.getDetails() != null) {
            req.getDetails().forEach(d -> saveDetail(saved, d));
        }
        return mapper.toResponse(incrementRepo.findById(saved.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public SalaryIncrementResponse update(Long id, SalaryIncrementRequest req) {
        SalaryIncrement increment = findIncrement(id);
        assertEditable(increment);
        increment.setCode(req.getCode());
        increment.setName(req.getName());
        increment.setType(req.getType());
        increment.setEffectiveMonth(req.getEffectiveMonth());
        increment.setRemarks(req.getRemarks());
        increment.setModifiedBy(usrRepo.getReferenceById(req.getModifiedBy()));

        // Replace details — delete children first (FAs), then details, then flush.
        // JPQL bulk deletes bypass Hibernate cascade, so order matters to satisfy FKs.
        faRepo.deleteAllByIncrement(increment);
        faRepo.flush();
        detailRepo.deleteAllByIncrement(increment);
        detailRepo.flush();
        increment.getDetails().clear();
        if (req.getDetails() != null) {
            req.getDetails().forEach(d -> saveDetail(increment, d));
        }
        return mapper.toResponse(incrementRepo.findById(id).orElseThrow());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SalaryIncrement increment = findIncrement(id);
        assertEditable(increment);
        incrementRepo.delete(increment);
    }

    @Override
    @Transactional
    public SalaryIncrementResponse approve(Long id) {
        SalaryIncrement increment = findIncrement(id);
        if (increment.getStatus() != IncrementStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT increments can be approved.");
        }
        increment.setStatus(IncrementStatus.APPROVED);
        return mapper.toResponse(incrementRepo.save(increment));
    }

    @Override
    @Transactional
    public SalaryIncrementResponse cancel(Long id) {
        SalaryIncrement increment = findIncrement(id);
        if (increment.getStatus() == IncrementStatus.EXPORTED) {
            throw new IllegalStateException("Exported increments cannot be cancelled.");
        }
        increment.setStatus(IncrementStatus.CANCELLED);
        return mapper.toResponse(incrementRepo.save(increment));
    }

    // ── Export ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SalaryIncrementResponse exportToPayroll(Long id) {
        SalaryIncrement increment = findIncrement(id);
        if (increment.getStatus() != IncrementStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED increments can be exported.");
        }

        String month = increment.getEffectiveMonth();

        for (SalaryIncrementDetail detail : increment.getDetails()) {
            if (Boolean.TRUE.equals(detail.getIsExported())) continue;

            Employee emp = detail.getEmployee();

            // Update basic salary on the employee master
            if (detail.getNewBasic() != null && detail.getNewBasic().compareTo(BigDecimal.ZERO) > 0) {
                emp.setBasicSalary(detail.getNewBasic());
                employeeRepo.save(emp);
            }

            // Update / create emp_fa records for each FA increment
            for (SalaryIncrementFa fa : detail.getFaIncrements()) {
                if (fa.getNewAmount() == null || fa.getNewAmount().compareTo(BigDecimal.ZERO) == 0) continue;

                List<EmployeeFixedAllowance> existing =
                        empFaRepo.findAllByEmployeeIdAndPayrollMonth(emp.getId(), month);

                EmployeeFixedAllowance empFa = existing.stream()
                        .filter(e -> e.getFixedAllowance().getId().equals(fa.getFixedAllowance().getId()))
                        .findFirst()
                        .orElseGet(() -> {
                            EmployeeFixedAllowance n = new EmployeeFixedAllowance();
                            n.setEmployee(emp);
                            n.setFixedAllowance(fa.getFixedAllowance());
                            n.setPayrollMonth(month);
                            n.setIsProcessed(false);
                            n.setCreatedBy(detail.getCreatedBy());
                            n.setModifiedBy(detail.getModifiedBy());
                            return n;
                        });

                empFa.setAmount(fa.getNewAmount());
                empFa.setModifiedBy(detail.getModifiedBy());
                empFaRepo.save(empFa);
            }

            detail.setIsExported(true);
            detail.setExportedDate(LocalDateTime.now());
            detailRepo.save(detail);
        }

        increment.setStatus(IncrementStatus.EXPORTED);
        return mapper.toResponse(incrementRepo.save(increment));
    }

    // ── Import ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SalaryIncrementResponse importFromPayroll(Long id) {
        SalaryIncrement increment = findIncrement(id);
        String month = increment.getEffectiveMonth();

        for (SalaryIncrementDetail detail : increment.getDetails()) {
            Employee emp = employeeRepo.findById(detail.getEmployee().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", detail.getEmployee().getId()));

            // Refresh current basic
            detail.setCurrentBasic(emp.getBasicSalary());
            detail.setNewBasic(emp.getBasicSalary().add(
                    detail.getIncrementBasic() != null ? detail.getIncrementBasic() : BigDecimal.ZERO));

            // Refresh FA current amounts
            List<EmployeeFixedAllowance> empFas =
                    empFaRepo.findAllByEmployeeIdAndPayrollMonth(emp.getId(), month);

            for (SalaryIncrementFa fa : detail.getFaIncrements()) {
                BigDecimal current = empFas.stream()
                        .filter(e -> e.getFixedAllowance().getId().equals(fa.getFixedAllowance().getId()))
                        .map(EmployeeFixedAllowance::getAmount)
                        .findFirst()
                        .orElse(BigDecimal.ZERO);

                fa.setCurrentAmount(current);
                fa.setNewAmount(current.add(
                        fa.getIncrementAmount() != null ? fa.getIncrementAmount() : BigDecimal.ZERO));
                faRepo.save(fa);
            }

            detailRepo.save(detail);
        }

        return mapper.toResponse(incrementRepo.findById(id).orElseThrow());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private SalaryIncrement findIncrement(Long id) {
        return incrementRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalaryIncrement", "id", id));
    }

    @Override
    @Transactional
    public SalaryIncrementResponse post(Long id) {
        SalaryIncrement increment = findIncrement(id);
        if (increment.getStatus() != IncrementStatus.EXPORTED) {
            throw new IllegalStateException("Only EXPORTED increments can be posted.");
        }
        increment.setStatus(IncrementStatus.POSTED);
        return mapper.toResponse(incrementRepo.save(increment));
    }

    private void assertEditable(SalaryIncrement increment) {
        if (increment.getStatus() == IncrementStatus.POSTED ||
            increment.getStatus() == IncrementStatus.CANCELLED) {
            throw new IllegalStateException("Posted or cancelled increments cannot be modified.");
        }
    }

    private void saveDetail(SalaryIncrement increment, SalaryIncrementDetailRequest req) {
        Employee emp = employeeRepo.getReferenceById(req.getEmpId());

        SalaryIncrementDetail detail = new SalaryIncrementDetail();
        detail.setIncrement(increment);
        detail.setEmployee(emp);
        detail.setCurrentBasic(req.getCurrentBasic() != null ? req.getCurrentBasic() : BigDecimal.ZERO);
        detail.setIncrementBasic(req.getIncrementBasic() != null ? req.getIncrementBasic() : BigDecimal.ZERO);
        detail.setNewBasic(req.getNewBasic() != null ? req.getNewBasic() : BigDecimal.ZERO);
        detail.setRemarks(req.getRemarks());
        detail.setIsExported(false);
        detail.setCreatedBy(usrRepo.getReferenceById(req.getCreatedBy()));
        detail.setModifiedBy(usrRepo.getReferenceById(req.getModifiedBy()));

        SalaryIncrementDetail saved = detailRepo.save(detail);

        if (req.getFaIncrements() != null) {
            for (SalaryIncrementFaRequest faReq : req.getFaIncrements()) {
                SalaryIncrementFa fa = new SalaryIncrementFa();
                fa.setDetail(saved);
                fa.setFixedAllowance(fixedAllowanceRepo.getReferenceById(faReq.getFaId()));
                fa.setCurrentAmount(faReq.getCurrentAmount() != null ? faReq.getCurrentAmount() : BigDecimal.ZERO);
                fa.setIncrementAmount(faReq.getIncrementAmount() != null ? faReq.getIncrementAmount() : BigDecimal.ZERO);
                fa.setNewAmount(faReq.getNewAmount() != null ? faReq.getNewAmount() : BigDecimal.ZERO);
                fa.setCreatedBy(usrRepo.getReferenceById(faReq.getCreatedBy()));
                fa.setModifiedBy(usrRepo.getReferenceById(faReq.getModifiedBy()));
                faRepo.save(fa);
            }
        }
    }
}
