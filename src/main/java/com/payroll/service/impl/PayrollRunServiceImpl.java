package com.payroll.service.impl;

import com.payroll.dto.response.PayrollRunDetailResponseDTO;
import com.payroll.dto.response.PayrollRunResponseDTO;
import com.payroll.dto.response.PayrollRunSummaryDTO;
import com.payroll.entity.*;
import com.payroll.enums.ComponentType;
import com.payroll.enums.PayrollRunStatus;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.repository.*;
import com.payroll.service.PayrollRunService;
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

    private final EmpPayrollRunRepository payrollRunRepository;
    private final EmpPayrollRunDetailRepository payrollRunDetailRepository;
    private final EmployeeRepository employeeRepository;
    private final UsrRepository usrRepository;

    // Component repositories
    private final EmployeeFixedAllowanceRepository empFaRepository;
    private final EmployeeFixedDeductionRepository empFdRepository;
    private final EmployeeVariableAllowanceRepository empVaRepository;
    private final EmployeeVariableDeductionRepository empVdRepository;
    private final EmployeeOvertimeRepository empOtRepository;
    private final EmployeeNopayRepository empNpRepository;

    private static final Sort ID_ASC = Sort.by("id").ascending();

    // ── Process ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PayrollRunResponseDTO processPayroll(Long empId, String payrollMonth, Long processedBy) {
        // Reject if already LOCKED
        if (payrollRunRepository.existsByEmployee_IdAndPayrollMonthAndStatus(
                empId, payrollMonth, PayrollRunStatus.LOCKED)) {
            throw new IllegalStateException(
                    "Payroll is already locked for employee " + empId + " for month " + payrollMonth);
        }

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", empId));

        Usr processedByUser = usrRepository.findById(processedBy)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", processedBy));

        // Delete existing DRAFT run for this month if any (re-process)
        payrollRunRepository.findByEmployee_IdAndPayrollMonth(empId, payrollMonth)
                .filter(r -> r.getStatus() == PayrollRunStatus.DRAFT)
                .ifPresent(payrollRunRepository::delete);

        // Collect component lines
        List<EmpPayrollRunDetail> details = new ArrayList<>();
        BigDecimal totalAllowances = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;

        // Fixed Allowances
        for (EmployeeFixedAllowance fa : empFaRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth)) {
            totalAllowances = totalAllowances.add(fa.getAmount());
            details.add(buildDetail(ComponentType.FA,
                    fa.getFixedAllowance().getId(),
                    fa.getFixedAllowance().getCode(),
                    fa.getFixedAllowance().getName(),
                    fa.getAmount(), null, null, processedByUser));
        }

        // Variable Allowances
        for (EmployeeVariableAllowance va : empVaRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth)) {
            totalAllowances = totalAllowances.add(va.getAmount());
            details.add(buildDetail(ComponentType.VA,
                    va.getVariableAllowance().getId(),
                    va.getVariableAllowance().getCode(),
                    va.getVariableAllowance().getName(),
                    va.getAmount(), null, null, processedByUser));
        }

        // Overtime
        for (EmployeeOvertime ot : empOtRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth)) {
            totalAllowances = totalAllowances.add(ot.getAmount());
            details.add(buildDetail(ComponentType.OT,
                    ot.getOvertime().getId(),
                    ot.getOvertime().getCode(),
                    ot.getOvertime().getName(),
                    ot.getAmount(), ot.getHours(), null, processedByUser));
        }

        // Fixed Deductions
        for (EmployeeFixedDeduction fd : empFdRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth)) {
            totalDeductions = totalDeductions.add(fd.getAmount());
            details.add(buildDetail(ComponentType.FD,
                    fd.getFixedDeduction().getId(),
                    fd.getFixedDeduction().getCode(),
                    fd.getFixedDeduction().getName(),
                    fd.getAmount(), null, null, processedByUser));
        }

        // Variable Deductions
        for (EmployeeVariableDeduction vd : empVdRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth)) {
            totalDeductions = totalDeductions.add(vd.getAmount());
            details.add(buildDetail(ComponentType.VD,
                    vd.getVariableDeduction().getId(),
                    vd.getVariableDeduction().getCode(),
                    vd.getVariableDeduction().getName(),
                    vd.getAmount(), null, null, processedByUser));
        }

        // No Pay
        for (EmployeeNopay np : empNpRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth)) {
            totalDeductions = totalDeductions.add(np.getAmount());
            details.add(buildDetail(ComponentType.NOPAY,
                    np.getNopayDays().getId(),
                    np.getNopayDays().getCode(),
                    np.getNopayDays().getName(),
                    np.getAmount(), null, np.getDays(), processedByUser));
        }

        BigDecimal basicSalary = employee.getBasicSalary() != null ? employee.getBasicSalary() : BigDecimal.ZERO;
        BigDecimal grossPay = basicSalary.add(totalAllowances);
        BigDecimal netPay = grossPay.subtract(totalDeductions);

        EmpPayrollRun run = EmpPayrollRun.builder()
                .employee(employee)
                .payrollMonth(payrollMonth)
                .basicSalary(basicSalary)
                .totalAllowances(totalAllowances)
                .totalDeductions(totalDeductions)
                .grossPay(grossPay)
                .netPay(netPay)
                .status(PayrollRunStatus.DRAFT)
                .createdBy(processedByUser)
                .modifiedBy(processedByUser)
                .build();

        details.forEach(d -> d.setPayrollRun(run));
        run.setDetails(details);

        EmpPayrollRun saved = payrollRunRepository.save(run);
        log.info("Payroll run created for empId={} month={} runId={}", empId, payrollMonth, saved.getId());

        return toResponseDTO(saved);
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

        // Mark component records as processed
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

        EmpPayrollRun locked = payrollRunRepository.save(run);
        log.info("Payroll run locked runId={} empId={} month={}", runId, empId, payrollMonth);

        return toResponseDTO(locked);
    }

    // ── Batch ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public List<PayrollRunSummaryDTO> processPayrollForMonth(String payrollMonth, Long processedBy) {
        List<Employee> activeEmployees = employeeRepository.findAllByIsActive(true, ID_ASC);
        List<PayrollRunSummaryDTO> results = new ArrayList<>();

        for (Employee emp : activeEmployees) {
            try {
                PayrollRunResponseDTO run = processPayroll(emp.getId(), payrollMonth, processedBy);
                results.add(toSummaryDTO(run));
            } catch (Exception ex) {
                log.warn("Skipped payroll for empId={} month={}: {}", emp.getId(), payrollMonth, ex.getMessage());
            }
        }

        return results;
    }

    // ── Queries ──────────────────────────────────────────────────────────────

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

    // ── Mapping helpers ──────────────────────────────────────────────────────

    private EmpPayrollRunDetail buildDetail(ComponentType type, Long componentId, String code, String name,
                                            BigDecimal amount, BigDecimal hours, BigDecimal days, Usr user) {
        return EmpPayrollRunDetail.builder()
                .componentType(type)
                .componentId(componentId)
                .componentCode(code)
                .componentName(name)
                .amount(amount)
                .hours(hours)
                .days(days)
                .createdBy(user)
                .modifiedBy(user)
                .build();
    }

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
                        .createdByUserName(d.getCreatedBy() != null ? d.getCreatedBy().getUserName() : null)
                        .createdDate(d.getCreatedDate())
                        .modifiedById(d.getModifiedBy() != null ? d.getModifiedBy().getId() : null)
                        .modifiedByUserName(d.getModifiedBy() != null ? d.getModifiedBy().getUserName() : null)
                        .modifiedDate(d.getModifiedDate())
                        .build())
                .collect(Collectors.toList());

        return PayrollRunResponseDTO.builder()
                .id(run.getId())
                .payrollMonth(run.getPayrollMonth())
                .status(run.getStatus())
                .empId(run.getEmployee().getId())
                .empCode(run.getEmployee().getEmployeeNo())
                .empName(run.getEmployee().getPayrollName())
                .basicSalary(run.getBasicSalary())
                .totalAllowances(run.getTotalAllowances())
                .totalDeductions(run.getTotalDeductions())
                .grossPay(run.getGrossPay())
                .netPay(run.getNetPay())
                .processedDate(run.getProcessedDate())
                .processedById(run.getProcessedBy() != null ? run.getProcessedBy().getId() : null)
                .processedByUserName(run.getProcessedBy() != null ? run.getProcessedBy().getUserName() : null)
                .createdById(run.getCreatedBy() != null ? run.getCreatedBy().getId() : null)
                .createdByUserName(run.getCreatedBy() != null ? run.getCreatedBy().getUserName() : null)
                .createdDate(run.getCreatedDate())
                .modifiedById(run.getModifiedBy() != null ? run.getModifiedBy().getId() : null)
                .modifiedByUserName(run.getModifiedBy() != null ? run.getModifiedBy().getUserName() : null)
                .modifiedDate(run.getModifiedDate())
                .details(detailDTOs)
                .build();
    }

    private PayrollRunSummaryDTO toSummaryFromEntity(EmpPayrollRun run) {
        return PayrollRunSummaryDTO.builder()
                .id(run.getId())
                .payrollMonth(run.getPayrollMonth())
                .status(run.getStatus())
                .empId(run.getEmployee().getId())
                .empCode(run.getEmployee().getEmployeeNo())
                .empName(run.getEmployee().getPayrollName())
                .basicSalary(run.getBasicSalary())
                .totalAllowances(run.getTotalAllowances())
                .totalDeductions(run.getTotalDeductions())
                .grossPay(run.getGrossPay())
                .netPay(run.getNetPay())
                .processedDate(run.getProcessedDate())
                .processedByUserName(run.getProcessedBy() != null ? run.getProcessedBy().getUserName() : null)
                .build();
    }

    private PayrollRunSummaryDTO toSummaryDTO(PayrollRunResponseDTO dto) {
        return PayrollRunSummaryDTO.builder()
                .id(dto.getId())
                .payrollMonth(dto.getPayrollMonth())
                .status(dto.getStatus())
                .empId(dto.getEmpId())
                .empCode(dto.getEmpCode())
                .empName(dto.getEmpName())
                .basicSalary(dto.getBasicSalary())
                .totalAllowances(dto.getTotalAllowances())
                .totalDeductions(dto.getTotalDeductions())
                .grossPay(dto.getGrossPay())
                .netPay(dto.getNetPay())
                .processedDate(dto.getProcessedDate())
                .processedByUserName(dto.getProcessedByUserName())
                .build();
    }
}
