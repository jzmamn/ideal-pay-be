package com.payroll.service.impl;

import com.payroll.dto.request.CorrectionDetailUpdateDTO;
import com.payroll.dto.response.PayrollRunDetailResponseDTO;
import com.payroll.dto.response.PayrollRunResponseDTO;
import com.payroll.dto.response.PayrollRunSummaryDTO;
import com.payroll.entity.*;
import com.payroll.entity.EmployeeSalaryAdvance;
import com.payroll.enums.ComponentType;
import com.payroll.enums.PayrollRunStatus;
import com.payroll.enums.RunType;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.repository.*;
import com.payroll.service.PayrollRunService;
import com.payroll.license.LicenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PayrollRunServiceImpl implements PayrollRunService {

    private final EmpPayrollRunRepository          payrollRunRepository;
    private final EmpPayrollRunDetailRepository    payrollRunDetailRepository;
    private final EmployeeRepository               employeeRepository;
    private final UsrRepository                    usrRepository;
    private final PayrollPeriodRepository          payrollPeriodRepository;
    private final PayrollEmployeeProcessorImpl     employeeProcessor;
    private final LicenseService                    licenseService;

    // Component repositories
    private final EmployeeFixedAllowanceRepository    empFaRepository;
    private final EmployeeFixedDeductionRepository    empFdRepository;
    private final EmployeeVariableAllowanceRepository empVaRepository;
    private final EmployeeVariableDeductionRepository empVdRepository;
    private final EmployeeOvertimeRepository          empOtRepository;
    private final EmployeeNopayRepository             empNpRepository;
    private final EmployeeSalaryAdvanceRepository     empSaRepository;

    private static final Sort ID_ASC = Sort.by("id").ascending();

    // ── Process ──────────────────────────────────────────────────────────────

    @Override
    public PayrollRunResponseDTO processPayroll(Long empId, String payrollMonth, Long processedBy) {
        licenseService.requirePayrollAllowed();
        // Delegate to the processor — runs in its own REQUIRES_NEW transaction
        return employeeProcessor.processOne(empId, payrollMonth, processedBy);
    }

    // ── Lock ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PayrollRunResponseDTO lockPayrollRun(Long runId, Long lockedBy) {
        EmpPayrollRun run = payrollRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", "id", runId));

        if (run.getStatus() == PayrollRunStatus.LOCKED) {
            throw new IllegalStateException("Payroll run " + runId + " is already locked");
        }

        Usr lockedByUser = usrRepository.findById(lockedBy)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", lockedBy));

        run.setStatus(PayrollRunStatus.LOCKED);
        run.setProcessedDate(LocalDateTime.now());
        run.setProcessedBy(lockedByUser);
        run.setModifiedBy(lockedByUser);

        // Mark all component records as processed
        LocalDateTime now = LocalDateTime.now();
        Long empId = run.getEmployee().getId();
        String payrollMonth = run.getPayrollMonth();

        List<EmployeeFixedAllowance> fas = empFaRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth);
        fas.forEach(fa -> { fa.setIsProcessed(true); fa.setProcessedDate(now); });
        empFaRepository.saveAll(fas);

        List<EmployeeFixedDeduction> fds = empFdRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth);
        fds.forEach(fd -> { fd.setIsProcessed(true); fd.setProcessedDate(now); });
        empFdRepository.saveAll(fds);

        List<EmployeeVariableAllowance> vas = empVaRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth);
        vas.forEach(va -> { va.setIsProcessed(true); va.setProcessedDate(now); });
        empVaRepository.saveAll(vas);

        List<EmployeeVariableDeduction> vds = empVdRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth);
        vds.forEach(vd -> { vd.setIsProcessed(true); vd.setProcessedDate(now); });
        empVdRepository.saveAll(vds);

        List<EmployeeOvertime> ots = empOtRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth);
        ots.forEach(ot -> { ot.setIsProcessed(true); ot.setProcessedDate(now); });
        empOtRepository.saveAll(ots);

        List<EmployeeNopay> nps = empNpRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth);
        nps.forEach(np -> { np.setIsProcessed(true); np.setProcessedDate(now); });
        empNpRepository.saveAll(nps);

        // Mark salary advances as processed (locked with payroll run)
        List<EmployeeSalaryAdvance> sas = empSaRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth);
        sas.forEach(sa -> { sa.setIsProcessed(true); sa.setProcessedDate(now); });
        empSaRepository.saveAll(sas);

        EmpPayrollRun locked = payrollRunRepository.save(run);
        log.info("Payroll run locked — runId={} empId={} month={}", runId, empId, payrollMonth);

        return toResponseDTO(locked);
    }

    // ── Batch ────────────────────────────────────────────────────────────────

    @Override
    @Transactional  // overrides class-level readOnly=true; license audit log needs a writable tx
    // Each employee still runs in its own REQUIRES_NEW transaction via employeeProcessor.processOne().
    // A failure for one employee never corrupts the Hibernate session for the next one.
    public List<PayrollRunSummaryDTO> processPayrollForMonth(String payrollMonth, Long processedBy) {
        licenseService.requirePayrollAllowed();
        List<Employee> activeEmployees = employeeRepository.findAllByIsActive(true, ID_ASC);
        List<PayrollRunSummaryDTO> results = new ArrayList<>();

        log.info("Batch payroll start — month={} employees={}", payrollMonth, activeEmployees.size());

        for (Employee emp : activeEmployees) {
            if (emp.getId() == null || emp.getId() <= 0) {
                log.debug("Skipping invalid empId={}", emp.getId());
                continue;
            }
            try {
                PayrollRunResponseDTO run = employeeProcessor.processOne(emp.getId(), payrollMonth, processedBy);
                results.add(toSummaryDTO(run));
            } catch (Exception ex) {
                log.warn("Skipped payroll for empId={} month={}: {}", emp.getId(), payrollMonth, ex.getMessage());
            }
        }

        log.info("Batch payroll done — month={} processed={}/{}", payrollMonth, results.size(), activeEmployees.size());
        return results;
    }

    // ── Correction ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PayrollRunResponseDTO createCorrectionRun(Long originalRunId, Long userId) {
        EmpPayrollRun original = payrollRunRepository.findById(originalRunId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", "id", originalRunId));

        if (original.getStatus() != PayrollRunStatus.LOCKED) {
            throw new IllegalStateException("Only LOCKED runs can be corrected. Run " + originalRunId
                    + " has status " + original.getStatus());
        }
        if (original.getRunType() == RunType.CORRECTION) {
            throw new IllegalStateException("Cannot create a correction of a correction run.");
        }

        Usr user = usrRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<EmpPayrollRunDetail> correctedDetails = original.getDetails().stream()
                .map(d -> EmpPayrollRunDetail.builder()
                        .componentType(d.getComponentType())
                        .componentId(d.getComponentId())
                        .componentCode(d.getComponentCode())
                        .componentName(d.getComponentName())
                        .amount(d.getAmount())
                        .hours(d.getHours())
                        .days(d.getDays())
                        .createdBy(user)
                        .modifiedBy(user)
                        .build())
                .collect(Collectors.toList());

        EmpPayrollRun correction = EmpPayrollRun.builder()
                .employee(original.getEmployee())
                .payrollMonth(original.getPayrollMonth())
                .basicSalary(original.getBasicSalary())
                .totalAllowances(original.getTotalAllowances())
                .totalDeductions(original.getTotalDeductions())
                .grossPay(original.getGrossPay())
                .netPay(original.getNetPay())
                .epfLiableBase(original.getEpfLiableBase())
                .taxableEarnings(original.getTaxableEarnings())
                .employeeEpf(original.getEmployeeEpf())
                .employerEpf(original.getEmployerEpf())
                .etf(original.getEtf())
                .payeTax(original.getPayeTax())
                .workingDays(original.getWorkingDays())
                .status(PayrollRunStatus.CORRECTION_DRAFT)
                .runType(RunType.CORRECTION)
                .parentRunId(originalRunId)
                .createdBy(user)
                .modifiedBy(user)
                .build();

        correctedDetails.forEach(d -> d.setPayrollRun(correction));
        correction.setDetails(correctedDetails);

        EmpPayrollRun saved = payrollRunRepository.save(correction);
        log.info("Correction run created — runId={} parentRunId={} empId={} month={}",
                saved.getId(), originalRunId, original.getEmployee().getId(), original.getPayrollMonth());
        return toResponseDTO(saved);
    }

    @Override
    @Transactional
    public PayrollRunResponseDTO updateCorrectionDetails(Long correctionRunId,
                                                         List<CorrectionDetailUpdateDTO> updates,
                                                         Long userId) {
        EmpPayrollRun run = payrollRunRepository.findById(correctionRunId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", "id", correctionRunId));

        if (run.getStatus() != PayrollRunStatus.CORRECTION_DRAFT) {
            throw new IllegalStateException("Only CORRECTION_DRAFT runs can be edited.");
        }

        Usr user = usrRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        run.getDetails().clear();
        payrollRunRepository.save(run);

        BigDecimal totalAllowances = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;

        for (CorrectionDetailUpdateDTO u : updates) {
            EmpPayrollRunDetail detail = EmpPayrollRunDetail.builder()
                    .payrollRun(run)
                    .componentType(u.getComponentType())
                    .componentId(u.getComponentId())
                    .componentCode(u.getComponentCode())
                    .componentName(u.getComponentName())
                    .amount(u.getAmount() != null ? u.getAmount() : BigDecimal.ZERO)
                    .hours(u.getHours())
                    .days(u.getDays())
                    .createdBy(user)
                    .modifiedBy(user)
                    .build();
            run.getDetails().add(detail);

            if (u.getComponentType() == ComponentType.FA
                    || u.getComponentType() == ComponentType.VA
                    || u.getComponentType() == ComponentType.OT) {
                totalAllowances = totalAllowances.add(detail.getAmount());
            }
            if (u.getComponentType() == ComponentType.FD
                    || u.getComponentType() == ComponentType.VD
                    || u.getComponentType() == ComponentType.NOPAY
                    || u.getComponentType() == ComponentType.EPF_EE
                    || u.getComponentType() == ComponentType.PAYE) {
                totalDeductions = totalDeductions.add(detail.getAmount());
            }
        }

        BigDecimal grossPay = run.getBasicSalary().add(totalAllowances);
        run.setTotalAllowances(totalAllowances);
        run.setTotalDeductions(totalDeductions);
        run.setGrossPay(grossPay);
        run.setNetPay(grossPay.subtract(totalDeductions));
        run.setModifiedBy(user);

        return toResponseDTO(payrollRunRepository.save(run));
    }

    @Override
    @Transactional
    public PayrollRunResponseDTO lockCorrectionRun(Long correctionRunId, Long userId) {
        EmpPayrollRun run = payrollRunRepository.findById(correctionRunId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", "id", correctionRunId));

        if (run.getStatus() != PayrollRunStatus.CORRECTION_DRAFT) {
            throw new IllegalStateException("Only CORRECTION_DRAFT runs can be locked.");
        }

        Usr user = usrRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        run.setStatus(PayrollRunStatus.CORRECTION_LOCKED);
        run.setProcessedDate(LocalDateTime.now());
        run.setProcessedBy(user);
        run.setModifiedBy(user);

        EmpPayrollRun locked = payrollRunRepository.save(run);
        log.info("Correction run locked — correctionRunId={} parentRunId={}", correctionRunId, run.getParentRunId());
        return toResponseDTO(locked);
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    @Override
    public List<PayrollRunSummaryDTO> getCorrectionsByOriginalRun(Long originalRunId) {
        return payrollRunRepository.findAllByParentRunId(originalRunId, ID_ASC)
                .stream().map(this::toSummaryFromEntity).collect(Collectors.toList());
    }

    @Override
    public PayrollRunResponseDTO getPayrollRunById(Long runId) {
        EmpPayrollRun run = payrollRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", "id", runId));
        return toResponseDTO(run);
    }

    @Override
    public List<PayrollRunSummaryDTO> getPayrollRunsByEmployee(Long empId) {
        return payrollRunRepository.findAllByEmployee_Id(empId, Sort.by("payrollMonth").descending())
                .stream().map(this::toSummaryFromEntity).collect(Collectors.toList());
    }

    @Override
    public List<PayrollRunSummaryDTO> getPayrollRunsByMonth(String payrollMonth) {
        return payrollRunRepository.findAllByPayrollMonth(payrollMonth, ID_ASC)
                .stream().map(this::toSummaryFromEntity).collect(Collectors.toList());
    }

    // ── Mapping helpers ───────────────────────────────────────────────────────

    private PayrollRunResponseDTO toResponseDTO(EmpPayrollRun run) {
        List<PayrollRunDetailResponseDTO> detailDTOs = run.getDetails().stream()
                .map(d -> PayrollRunDetailResponseDTO.builder()
                        .id(d.getId())
                        .componentType(d.getComponentType())
                        .componentId(d.getComponentId())
                        .componentCode(d.getComponentCode())
                        .componentName(d.getComponentName())
                        .amount(d.getAmount())
                        .hours(d.getHours())
                        .days(d.getDays())
                        .createdById(d.getCreatedBy() != null ? d.getCreatedBy().getId() : null)
                        .createdByUserName(d.getCreatedBy() != null ? d.getCreatedBy().getUsername() : null)
                        .createdDate(d.getCreatedDate())
                        .modifiedById(d.getModifiedBy() != null ? d.getModifiedBy().getId() : null)
                        .modifiedByUserName(d.getModifiedBy() != null ? d.getModifiedBy().getUsername() : null)
                        .modifiedDate(d.getModifiedDate())
                        .build())
                .collect(Collectors.toList());

        return PayrollRunResponseDTO.builder()
                .id(run.getId())
                .payrollMonth(run.getPayrollMonth())
                .status(run.getStatus())
                .runType(run.getRunType())
                .parentRunId(run.getParentRunId())
                .empId(run.getEmployee().getId())
                .empCode(run.getEmployee().getEmployeeNo())
                .empName(run.getEmployee().getPayrollName())
                .basicSalary(run.getBasicSalary())
                .totalAllowances(run.getTotalAllowances())
                .totalDeductions(run.getTotalDeductions())
                .grossPay(run.getGrossPay())
                .netPay(run.getNetPay())
                .epfLiableBase(run.getEpfLiableBase())
                .taxableEarnings(run.getTaxableEarnings())
                .employeeEpf(run.getEmployeeEpf())
                .employerEpf(run.getEmployerEpf())
                .etf(run.getEtf())
                .payeTax(run.getPayeTax())
                .workingDays(run.getWorkingDays())
                .processedDate(run.getProcessedDate())
                .processedById(run.getProcessedBy() != null ? run.getProcessedBy().getId() : null)
                .processedByUserName(run.getProcessedBy() != null ? run.getProcessedBy().getUsername() : null)
                .createdById(run.getCreatedBy() != null ? run.getCreatedBy().getId() : null)
                .createdByUserName(run.getCreatedBy() != null ? run.getCreatedBy().getUsername() : null)
                .createdDate(run.getCreatedDate())
                .modifiedById(run.getModifiedBy() != null ? run.getModifiedBy().getId() : null)
                .modifiedByUserName(run.getModifiedBy() != null ? run.getModifiedBy().getUsername() : null)
                .modifiedDate(run.getModifiedDate())
                .details(detailDTOs)
                .build();
    }

    private java.math.BigDecimal sumSaDetails(EmpPayrollRun run) {
        return run.getDetails().stream()
                .filter(d -> d.getComponentType() == ComponentType.SA)
                .map(d -> d.getAmount() != null ? d.getAmount() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    private PayrollRunSummaryDTO toSummaryFromEntity(EmpPayrollRun run) {
        return PayrollRunSummaryDTO.builder()
                .id(run.getId())
                .payrollMonth(run.getPayrollMonth())
                .status(run.getStatus())
                .runType(run.getRunType())
                .parentRunId(run.getParentRunId())
                .empId(run.getEmployee().getId())
                .empCode(run.getEmployee().getEmployeeNo())
                .empName(run.getEmployee().getPayrollName())
                .basicSalary(run.getBasicSalary())
                .totalAllowances(run.getTotalAllowances())
                .totalDeductions(run.getTotalDeductions())
                .grossPay(run.getGrossPay())
                .netPay(run.getNetPay())
                .epfLiableBase(run.getEpfLiableBase())
                .taxableEarnings(run.getTaxableEarnings())
                .employeeEpf(run.getEmployeeEpf())
                .employerEpf(run.getEmployerEpf())
                .etf(run.getEtf())
                .payeTax(run.getPayeTax())
                .workingDays(run.getWorkingDays())
                .salaryAdvanceAmount(sumSaDetails(run))
                .processedDate(run.getProcessedDate())
                .processedByUserName(run.getProcessedBy() != null ? run.getProcessedBy().getUsername() : null)
                .build();
    }

    private PayrollRunSummaryDTO toSummaryDTO(PayrollRunResponseDTO dto) {
        return PayrollRunSummaryDTO.builder()
                .id(dto.getId())
                .payrollMonth(dto.getPayrollMonth())
                .status(dto.getStatus())
                .runType(dto.getRunType())
                .parentRunId(dto.getParentRunId())
                .empId(dto.getEmpId())
                .empCode(dto.getEmpCode())
                .empName(dto.getEmpName())
                .basicSalary(dto.getBasicSalary())
                .totalAllowances(dto.getTotalAllowances())
                .totalDeductions(dto.getTotalDeductions())
                .grossPay(dto.getGrossPay())
                .netPay(dto.getNetPay())
                .epfLiableBase(dto.getEpfLiableBase())
                .taxableEarnings(dto.getTaxableEarnings())
                .employeeEpf(dto.getEmployeeEpf())
                .employerEpf(dto.getEmployerEpf())
                .etf(dto.getEtf())
                .payeTax(dto.getPayeTax())
                .workingDays(dto.getWorkingDays())
                .salaryAdvanceAmount(
                    dto.getDetails() != null
                        ? dto.getDetails().stream()
                            .filter(d -> com.payroll.enums.ComponentType.SA.name().equals(
                                d.getComponentType() != null ? d.getComponentType().name() : ""))
                            .map(d -> d.getAmount() != null ? d.getAmount() : java.math.BigDecimal.ZERO)
                            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                        : java.math.BigDecimal.ZERO)
                .processedDate(dto.getProcessedDate())
                .processedByUserName(dto.getProcessedByUserName())
                .build();
    }
}

