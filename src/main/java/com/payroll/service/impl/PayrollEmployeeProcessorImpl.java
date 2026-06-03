package com.payroll.service.impl;

import com.payroll.dto.response.PayrollRunResponseDTO;
import com.payroll.entity.*;
import com.payroll.enums.PayrollRunStatus;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.repository.*;
import com.payroll.repository.EmployeeSalaryAdvanceRepository;
import com.payroll.service.ComponentLine;
import com.payroll.service.FormulaEngineService;
import com.payroll.service.SalaryCalculationEngineService;
import com.payroll.service.SalaryCalculationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.payroll.dto.response.PayrollRunDetailResponseDTO;
import com.payroll.enums.RunType;

/**
 * Processes payroll for a SINGLE employee in its own independent transaction
 * (Propagation.REQUIRES_NEW).
 *
 * This is intentionally a separate Spring bean so that when
 * {@link PayrollRunServiceImpl#processPayrollForMonth} iterates employees,
 * each call goes through the Spring proxy and gets a fresh transaction.
 * A failure for one employee rolls back only that employee's transaction
 * and leaves the Hibernate session clean for the next one.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollEmployeeProcessorImpl {

    private final EmpPayrollRunRepository          payrollRunRepository;
    private final EmployeeRepository               employeeRepository;
    private final UsrRepository                    usrRepository;
    private final PayrollPeriodRepository          payrollPeriodRepository;
    private final SalaryCalculationEngineService   calculationEngine;

    private final EmployeeFixedAllowanceRepository    empFaRepository;
    private final EmployeeFixedDeductionRepository    empFdRepository;
    private final EmployeeVariableAllowanceRepository empVaRepository;
    private final EmployeeVariableDeductionRepository empVdRepository;
    private final EmployeeOvertimeRepository          empOtRepository;
    private final EmployeeNopayRepository             empNpRepository;
    private final EmployeeSalaryAdvanceRepository     empSaRepository;

    /**
     * Processes payroll for one employee in an independent transaction.
     * If this method throws, only this employee's work is rolled back.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PayrollRunResponseDTO processOne(Long empId, String payrollMonth, Long processedBy) {

        // Reject if already LOCKED
        if (payrollRunRepository.existsByEmployee_IdAndPayrollMonthAndStatus(
                empId, payrollMonth, PayrollRunStatus.LOCKED)) {
            throw new IllegalStateException(
                    "Payroll already locked for employee " + empId + " month " + payrollMonth);
        }

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", empId));

        Usr processedByUser = usrRepository.findById(processedBy)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", processedBy));

        // Delete existing DRAFT for this month and flush immediately
        // so the DELETE SQL executes before the new INSERT
        payrollRunRepository.findByEmployee_IdAndPayrollMonth(empId, payrollMonth)
                .filter(r -> r.getStatus() == PayrollRunStatus.DRAFT)
                .ifPresent(existing -> {
                    payrollRunRepository.delete(existing);
                    payrollRunRepository.flush();
                });

        // Load components
        List<EmployeeFixedAllowance>    faList = empFaRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth);
        List<EmployeeVariableAllowance> vaList = empVaRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth);
        List<EmployeeOvertime>          otList = empOtRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth);
        List<EmployeeFixedDeduction>    fdList = empFdRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth);
        List<EmployeeVariableDeduction> vdList = empVdRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth);
        List<EmployeeNopay>             npList = empNpRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth);
        List<EmployeeSalaryAdvance>     saList = empSaRepository.findAllByEmployeeIdAndPayrollMonth(empId, payrollMonth);

        int workingDays = payrollPeriodRepository.findByPeriodMonth(payrollMonth)
                .map(PayrollPeriod::getWorkingDays)
                .orElse(26);

        SalaryCalculationResult result = calculationEngine.calculate(
                employee, workingDays, faList, vaList, otList, fdList, vdList, npList, saList);

        List<EmpPayrollRunDetail> details = result.getLines().stream()
                .map(line -> buildDetail(line, processedByUser))
                .collect(Collectors.toList());

        EmpPayrollRun run = EmpPayrollRun.builder()
                .employee(employee)
                .payrollMonth(payrollMonth)
                .basicSalary(result.getBasicSalary())
                .totalAllowances(result.getTotalAllowances())
                .totalDeductions(result.getTotalDeductions())
                .grossPay(result.getGrossPay())
                .netPay(result.getNetPay())
                .epfLiableBase(result.getEpfLiableBase())
                .employeeEpf(result.getEmployeeEpf())
                .employerEpf(result.getEmployerEpf())
                .etf(result.getEtf())
                .payeTax(result.getPayeTax())
                .workingDays(result.getWorkingDays())
                .status(PayrollRunStatus.DRAFT)
                .createdBy(processedByUser)
                .modifiedBy(processedByUser)
                .build();

        details.forEach(d -> d.setPayrollRun(run));
        run.setDetails(details);

        EmpPayrollRun saved = payrollRunRepository.save(run);
        log.info("Payroll processed — empId={} month={} runId={} net={}",
                empId, payrollMonth, saved.getId(), result.getNetPay());

        return toResponseDTO(saved);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private EmpPayrollRunDetail buildDetail(ComponentLine line, Usr user) {
        return EmpPayrollRunDetail.builder()
                .componentType(line.getComponentType())
                .componentId(line.getComponentId())
                .componentCode(line.getComponentCode())
                .componentName(line.getComponentName())
                .amount(line.getAmount())
                .hours(line.getHours())
                .days(line.getDays())
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
                .employeeEpf(run.getEmployeeEpf())
                .employerEpf(run.getEmployerEpf())
                .etf(run.getEtf())
                .payeTax(run.getPayeTax())
                .workingDays(run.getWorkingDays())
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
}
