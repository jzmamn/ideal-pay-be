package com.payroll.service.impl;

import com.payroll.dto.request.BonusBatchActionRequestDTO;
import com.payroll.dto.request.BonusEntryAdjustRequestDTO;
import com.payroll.dto.request.BonusProcessingCalculateRequestDTO;
import com.payroll.dto.response.*;
import com.payroll.entity.*;
import com.payroll.enums.BonusStatus;
import com.payroll.enums.BonusCalculationMethod;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.repository.*;
import com.payroll.service.BonusProcessingService;
import com.payroll.service.FormulaEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BonusProcessingServiceImpl implements BonusProcessingService {

    private final BonusProcessingBatchRepository batchRepository;
    private final EmployeeBonusRepository        empBonusRepository;
    private final BonusRepository                bonusRepository;
    private final EmployeeRepository             employeeRepository;
    private final UsrRepository                  usrRepository;
    private final FormulaEngineService           formulaEngineService;

    // ── Calculate ─────────────────────────────────────────────────────────────

    @Override
    public BonusProcessingBatchResponseDTO calculate(BonusProcessingCalculateRequestDTO req) {
        Bonus bonus = bonusRepository.findById(req.getBonusId())
                .orElseThrow(() -> new ResourceNotFoundException("Bonus", "id", req.getBonusId()));

        if (!Boolean.TRUE.equals(bonus.getIsActive())) {
            throw new IllegalArgumentException("Bonus type '" + bonus.getName() + "' is inactive");
        }
        if (batchRepository.existsByBonusIdAndPayrollMonthAndStatusIn(
                bonus.getId(), req.getPayrollMonth(),
                List.of(BonusStatus.PENDING, BonusStatus.APPROVED, BonusStatus.PROCESSED))) {
            throw new IllegalStateException("An active bonus batch already exists for bonus '"
                    + bonus.getName() + "' and payroll month " + req.getPayrollMonth());
        }

        List<Employee> employees = resolveEmployees(req);
        if (employees.isEmpty()) {
            throw new IllegalArgumentException("No eligible employees found for the given criteria");
        }

        Usr createdBy  = usrRepository.getReferenceById(req.getCreatedBy());
        Usr modifiedBy = usrRepository.getReferenceById(req.getModifiedBy());

        // Create the batch header
        BonusProcessingBatch batch = BonusProcessingBatch.builder()
                .bonus(bonus)
                .payrollMonth(req.getPayrollMonth())
                .status(BonusStatus.PENDING)
                .employeeCount(0)
                .totalAmount(BigDecimal.ZERO)
                .notes(req.getNotes())
                .createdBy(createdBy)
                .modifiedBy(modifiedBy)
                .build();
        batch = batchRepository.save(batch);
        final BonusProcessingBatch savedBatch = batch;

        // Calculate and persist one EmployeeBonus row per employee
        BigDecimal runningTotal = BigDecimal.ZERO;
        List<EmployeeBonus> entries = new ArrayList<>();

        for (Employee emp : employees) {
            BigDecimal calculated = computeAmount(bonus, emp, req);
            String     expression = Boolean.TRUE.equals(bonus.getFormulaEnabled()) ? bonus.getFormula() : "fixed";

            EmployeeBonus entry = empBonusRepository
                    .findByEmployeeIdAndPayrollMonthAndBonusId(emp.getId(), req.getPayrollMonth(), bonus.getId())
                    .map(existing -> {
                        existing.setProcessingBatch(savedBatch);
                        existing.setAmount(calculated);
                        existing.setFormulaExpression(expression);
                        existing.setFormulaResult(calculated);
                        existing.setAdjustedAmount(null);
                        existing.setStatus(BonusStatus.PENDING);
                        existing.setIsProcessed(false);
                        existing.setModifiedBy(modifiedBy);
                        return existing;
                    })
                    .orElseGet(() -> EmployeeBonus.builder()
                            .employee(emp)
                            .bonus(bonus)
                            .processingBatch(savedBatch)
                            .payrollMonth(req.getPayrollMonth())
                            .amount(calculated)
                            .formulaExpression(expression)
                            .formulaResult(calculated)
                            .status(BonusStatus.PENDING)
                            .isProcessed(false)
                            .createdBy(createdBy)
                            .modifiedBy(modifiedBy)
                            .build());

            entries.add(empBonusRepository.save(entry));
            runningTotal = runningTotal.add(calculated);
        }

        // Update batch totals
        batch.setEmployeeCount(entries.size());
        batch.setTotalAmount(runningTotal);
        batch = batchRepository.save(batch);

        return toBatchResponse(batch, entries);
    }

    // ── List / Get ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<BonusProcessingBatchResponseDTO> getAllBatches(String payrollMonth, String status) {
        Sort sort = Sort.by("id").descending();

        List<BonusProcessingBatch> batches;
        if (payrollMonth != null && !payrollMonth.isBlank()) {
            batches = batchRepository.findAllByPayrollMonth(payrollMonth, sort);
        } else if (status != null && !status.isBlank() && !status.equalsIgnoreCase("all")) {
            BonusStatus bs = parseStatus(status);
            batches = batchRepository.findAllByStatus(bs, sort);
        } else {
            batches = batchRepository.findAll(sort);
        }

        return batches.stream()
                .map(b -> toBatchResponse(b, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BonusProcessingBatchResponseDTO getBatchById(Long batchId) {
        BonusProcessingBatch batch = batchRepository.findByIdWithDetails(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("BonusProcessingBatch", "id", batchId));
        List<EmployeeBonus> entries = empBonusRepository.findBatchEntriesWithDetails(batchId);
        return toBatchResponse(batch, entries);
    }

    // ── Adjust ────────────────────────────────────────────────────────────────

    @Override
    public EmployeeBonusProcessingRowDTO adjustEntry(Long batchId, Long entryId,
                                                     BonusEntryAdjustRequestDTO req) {
        BonusProcessingBatch batch = requireBatch(batchId);
        if (batch.getStatus() != BonusStatus.PENDING) {
            throw new IllegalStateException("Only PENDING bonus batches can be adjusted");
        }

        EmployeeBonus entry = empBonusRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeBonus", "id", entryId));
        if (!entry.getProcessingBatch().getId().equals(batchId)) {
            throw new IllegalArgumentException("Entry " + entryId + " does not belong to batch " + batchId);
        }

        entry.setAdjustedAmount(req.getAdjustedAmount());
        entry.setNote(req.getNote());
        entry.setModifiedBy(usrRepository.getReferenceById(req.getModifiedBy()));
        entry = empBonusRepository.save(entry);

        // Recalculate batch total
        recalculateBatchTotal(batch);

        return toRowDTO(entry);
    }

    // ── Approve ───────────────────────────────────────────────────────────────

    @Override
    public BonusProcessingBatchResponseDTO approveBatch(Long batchId, BonusBatchActionRequestDTO req) {
        BonusProcessingBatch batch = requireBatch(batchId);
        if (batch.getStatus() != BonusStatus.PENDING) {
            throw new IllegalStateException("Only PENDING batches can be approved (current: " + batch.getStatus() + ")");
        }

        Usr approvedBy = usrRepository.getReferenceById(req.getActingUserId());
        LocalDateTime now = LocalDateTime.now();

        // Transition each entry
        List<EmployeeBonus> entries = empBonusRepository.findBatchEntriesWithDetails(batchId);
        for (EmployeeBonus entry : entries) {
            entry.setStatus(BonusStatus.APPROVED);
            entry.setApprovedBy(approvedBy);
            entry.setApprovedDate(now);
            entry.setModifiedBy(approvedBy);
        }
        empBonusRepository.saveAll(entries);

        // Transition batch
        batch.setStatus(BonusStatus.APPROVED);
        batch.setApprovedBy(approvedBy);
        batch.setApprovedDate(now);
        batch.setModifiedBy(approvedBy);
        if (req.getNotes() != null) batch.setNotes(req.getNotes());
        batch = batchRepository.save(batch);

        return toBatchResponse(batch, entries);
    }

    // ── Process ───────────────────────────────────────────────────────────────

    @Override
    public BonusProcessingBatchResponseDTO processBatch(Long batchId, BonusBatchActionRequestDTO req) {
        BonusProcessingBatch batch = requireBatch(batchId);
        if (batch.getStatus() != BonusStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED batches can be processed (current: " + batch.getStatus() + ")");
        }

        Usr processedBy = usrRepository.getReferenceById(req.getActingUserId());
        LocalDateTime now = LocalDateTime.now();

        List<EmployeeBonus> entries = empBonusRepository.findBatchEntriesWithDetails(batchId);
        for (EmployeeBonus entry : entries) {
            entry.setStatus(BonusStatus.PROCESSED);
            entry.setIsProcessed(true);
            entry.setProcessedDate(now);
            entry.setModifiedBy(processedBy);
        }
        empBonusRepository.saveAll(entries);

        batch.setStatus(BonusStatus.PROCESSED);
        batch.setProcessedBy(processedBy);
        batch.setProcessedDate(now);
        batch.setModifiedBy(processedBy);
        if (req.getNotes() != null) batch.setNotes(req.getNotes());
        batch = batchRepository.save(batch);

        return toBatchResponse(batch, entries);
    }

    // ── Cancel ────────────────────────────────────────────────────────────────

    @Override
    public BonusProcessingBatchResponseDTO cancelBatch(Long batchId, BonusBatchActionRequestDTO req) {
        BonusProcessingBatch batch = requireBatch(batchId);
        if (batch.getStatus() == BonusStatus.PROCESSED) {
            throw new IllegalStateException("Cannot cancel a PROCESSED batch");
        }

        Usr actingUser = usrRepository.getReferenceById(req.getActingUserId());

        List<EmployeeBonus> entries = empBonusRepository.findBatchEntriesWithDetails(batchId);
        for (EmployeeBonus entry : entries) {
            entry.setStatus(BonusStatus.CANCELLED);
            entry.setModifiedBy(actingUser);
        }
        empBonusRepository.saveAll(entries);

        batch.setStatus(BonusStatus.CANCELLED);
        batch.setModifiedBy(actingUser);
        if (req.getNotes() != null) batch.setNotes(req.getNotes());
        batch = batchRepository.save(batch);

        return toBatchResponse(batch, entries);
    }

    // ── Reports ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<BonusSummaryReportDTO> getSummaryReport(String payrollMonth) {
        Sort sort = Sort.by("id").ascending();
        List<BonusProcessingBatch> batches = payrollMonth != null && !payrollMonth.isBlank()
                ? batchRepository.findAllByPayrollMonth(payrollMonth, sort)
                : batchRepository.findAll(sort);

        return batches.stream().map(b -> BonusSummaryReportDTO.builder()
                .bonusCode(b.getBonus().getCode())
                .bonusName(b.getBonus().getName())
                .payrollMonth(b.getPayrollMonth())
                .status(b.getStatus().name())
                .employeeCount(b.getEmployeeCount())
                .totalAmount(b.getTotalAmount())
                .build()).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeBonusProcessingRowDTO> getEmployeeReport(String payrollMonth, Long bonusId) {
        List<EmployeeBonus> rows = payrollMonth != null && !payrollMonth.isBlank()
                ? empBonusRepository.findByPayrollMonthWithDetails(payrollMonth)
                : empBonusRepository.findAll(Sort.by("id").ascending());

        return rows.stream()
                .filter(e -> bonusId == null || (e.getBonus() != null && e.getBonus().getId().equals(bonusId)))
                .filter(e -> e.getProcessingBatch() != null)
                .map(this::toRowDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BonusDepartmentReportDTO> getDepartmentReport(String payrollMonth) {
        List<EmployeeBonus> rows = payrollMonth != null && !payrollMonth.isBlank()
                ? empBonusRepository.findByPayrollMonthWithDetails(payrollMonth)
                : empBonusRepository.findAll(Sort.by("id").ascending());

        Map<String, List<EmployeeBonus>> grouped = rows.stream()
                .filter(e -> e.getProcessingBatch() != null)
                .collect(Collectors.groupingBy(e -> {
                    Employee emp = e.getEmployee();
                    return emp.getDepartment() != null ? emp.getDepartment().getName() : "Unassigned";
                }));

        return grouped.entrySet().stream().map(entry -> {
            List<EmployeeBonus> group = entry.getValue();
            BigDecimal total = group.stream()
                    .map(e -> effectiveAmount(e))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            String bonusName = group.get(0).getBonus() != null ? group.get(0).getBonus().getName() : "—";
            String month = group.get(0).getPayrollMonth();
            return BonusDepartmentReportDTO.builder()
                    .departmentName(entry.getKey())
                    .bonusName(bonusName)
                    .payrollMonth(month)
                    .employeeCount(group.size())
                    .totalAmount(total)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BonusApprovalReportDTO> getApprovalReport(String payrollMonth) {
        Sort sort = Sort.by("status").ascending().and(Sort.by("id").ascending());
        List<BonusProcessingBatch> batches = payrollMonth != null && !payrollMonth.isBlank()
                ? batchRepository.findAllByPayrollMonth(payrollMonth, sort)
                : batchRepository.findAll(sort);

        return batches.stream().map(b -> BonusApprovalReportDTO.builder()
                .batchId(b.getId())
                .bonusCode(b.getBonus().getCode())
                .bonusName(b.getBonus().getName())
                .payrollMonth(b.getPayrollMonth())
                .status(b.getStatus().name())
                .employeeCount(b.getEmployeeCount())
                .totalAmount(b.getTotalAmount())
                .approvedByUserName(b.getApprovedBy() != null ? b.getApprovedBy().getUserName() : null)
                .approvedDate(b.getApprovedDate() != null ? b.getApprovedDate().toString() : null)
                .createdByUserName(b.getCreatedBy() != null ? b.getCreatedBy().getUserName() : null)
                .createdDate(b.getCreatedDate() != null ? b.getCreatedDate().toString() : null)
                .build()).collect(Collectors.toList());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private List<Employee> resolveEmployees(BonusProcessingCalculateRequestDTO req) {
        if (req.getEmployeeIds() != null && !req.getEmployeeIds().isEmpty()) {
            List<Employee> selected = employeeRepository.findAllById(req.getEmployeeIds());
            if (selected.size() != new HashSet<>(req.getEmployeeIds()).size()) {
                throw new IllegalArgumentException("One or more selected employees do not exist");
            }
            if (selected.stream().anyMatch(e -> !Boolean.TRUE.equals(e.getIsActive()))) {
                throw new IllegalArgumentException("Inactive employees cannot be included in bonus processing");
            }
            return selected;
        }

        // Load all active employees then apply optional filters
        List<Employee> all = employeeRepository.findAllByIsActive(true, Sort.by("payrollName").ascending());

        return all.stream()
                .filter(e -> req.getDepartmentId()   == null || (e.getDepartment()  != null && e.getDepartment().getId().equals(req.getDepartmentId())))
                .filter(e -> req.getBranchId()       == null || (e.getBranch()      != null && e.getBranch().getId().equals(req.getBranchId())))
                .filter(e -> req.getDesignationId()  == null || (e.getDesignation() != null && e.getDesignation().getId().equals(req.getDesignationId())))
                .filter(e -> req.getGradeId()        == null || (e.getGrade()       != null && e.getGrade().getId().equals(req.getGradeId())))
                .filter(e -> req.getEmployeeTypeId() == null || (e.getEmployeeType() != null && e.getEmployeeType().getId().equals(req.getEmployeeTypeId())))
                .collect(Collectors.toList());
    }

    private BigDecimal computeAmount(Bonus bonus, Employee emp,
                                     BonusProcessingCalculateRequestDTO req) {
        if (bonus.getCalculationMethod() == BonusCalculationMethod.FORMULA_BASED
                && bonus.getFormula() != null
                && !bonus.getFormula().isBlank()) {

            Map<String, Object> ctx = buildFormulaContext(emp, req);
            BigDecimal result = formulaEngineService.evaluate(bonus.getFormula(), ctx);
            if (result.signum() < 0) {
                throw new IllegalArgumentException("Bonus formula produced a negative amount for employee "
                        + emp.getEmployeeNo());
            }
            return result.setScale(2, RoundingMode.HALF_UP);
        }

        // Fixed amount — use the value from the request
        BigDecimal fixedAmount = req.getFixedAmount();
        if (fixedAmount == null) {
            throw new IllegalStateException("No fixed amount is configured for bonus '" + bonus.getName() + "'");
        }
        return fixedAmount.setScale(2, RoundingMode.HALF_UP);
    }

    private Map<String, Object> buildFormulaContext(Employee emp,
                                                    BonusProcessingCalculateRequestDTO req) {
        Map<String, Object> ctx = new HashMap<>();
        BigDecimal basicSalary = emp.getBasicSalary() != null ? emp.getBasicSalary() : BigDecimal.ZERO;
        long servicePeriod = emp.getJoinedDate() != null
                ? ChronoUnit.MONTHS.between(emp.getJoinedDate(), LocalDate.now()) : 0L;
        ctx.put("basicSalary",   basicSalary);
        ctx.put("BASIC_SALARY",  basicSalary);
        ctx.put("workingDays",   26);
        ctx.put("WORKING_DAYS",  26);
        ctx.put("workedDays",    26);
        ctx.put("WORKED_DAYS",   26);
        ctx.put("nopayDays",     0);
        ctx.put("NOPAY_DAYS",    0);
        ctx.put("otHours",       BigDecimal.ZERO);
        ctx.put("OT_HOURS",      BigDecimal.ZERO);
        ctx.put("servicePeriod", servicePeriod);
        ctx.put("SERVICE_PERIOD", servicePeriod);

        // Caller-supplied context overrides defaults
        if (req.getFormulaContext() != null) {
            ctx.putAll(req.getFormulaContext());
        }
        if (req.getEmployeeFormulaContexts() != null
                && req.getEmployeeFormulaContexts().get(emp.getId()) != null) {
            ctx.putAll(req.getEmployeeFormulaContexts().get(emp.getId()));
        }
        return ctx;
    }

    private void recalculateBatchTotal(BonusProcessingBatch batch) {
        List<EmployeeBonus> entries = empBonusRepository.findAllByProcessingBatchId(
                batch.getId(), Sort.by("id").ascending());
        BigDecimal total = entries.stream()
                .map(this::effectiveAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        batch.setTotalAmount(total);
        batchRepository.save(batch);
    }

    private BigDecimal effectiveAmount(EmployeeBonus e) {
        return e.getAdjustedAmount() != null ? e.getAdjustedAmount() : e.getAmount();
    }

    private BonusStatus parseStatus(String status) {
        try {
            return BonusStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid status value: " + status
                    + ". Accepted: PENDING, APPROVED, PROCESSED, CANCELLED");
        }
    }

    private BonusProcessingBatch requireBatch(Long batchId) {
        return batchRepository.findByIdWithDetails(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("BonusProcessingBatch", "id", batchId));
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private BonusProcessingBatchResponseDTO toBatchResponse(BonusProcessingBatch b,
                                                            List<EmployeeBonus> entries) {
        Bonus bonus = b.getBonus();
        BonusProcessingBatchResponseDTO dto = BonusProcessingBatchResponseDTO.builder()
                .id(b.getId())
                .payrollMonth(b.getPayrollMonth())
                .status(b.getStatus())
                .employeeCount(b.getEmployeeCount())
                .totalAmount(b.getTotalAmount())
                .notes(b.getNotes())
                .bonusId(bonus.getId())
                .bonusCode(bonus.getCode())
                .bonusName(bonus.getName())
                .calculationMethod(bonus.getCalculationMethod())
                .formulaEnabled(bonus.getFormulaEnabled())
                .formula(bonus.getFormula())
                .createdById(b.getCreatedBy() != null ? b.getCreatedBy().getId() : null)
                .createdByUserName(b.getCreatedBy() != null ? b.getCreatedBy().getUserName() : null)
                .createdDate(b.getCreatedDate())
                .approvedById(b.getApprovedBy() != null ? b.getApprovedBy().getId() : null)
                .approvedByUserName(b.getApprovedBy() != null ? b.getApprovedBy().getUserName() : null)
                .approvedDate(b.getApprovedDate())
                .processedById(b.getProcessedBy() != null ? b.getProcessedBy().getId() : null)
                .processedByUserName(b.getProcessedBy() != null ? b.getProcessedBy().getUserName() : null)
                .processedDate(b.getProcessedDate())
                .modifiedById(b.getModifiedBy() != null ? b.getModifiedBy().getId() : null)
                .modifiedByUserName(b.getModifiedBy() != null ? b.getModifiedBy().getUserName() : null)
                .modifiedDate(b.getModifiedDate())
                .build();

        if (entries != null) {
            dto.setEntries(entries.stream().map(this::toRowDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    private EmployeeBonusProcessingRowDTO toRowDTO(EmployeeBonus e) {
        Employee emp  = e.getEmployee();
        BigDecimal eff = effectiveAmount(e);
        return EmployeeBonusProcessingRowDTO.builder()
                .id(e.getId())
                .empId(emp.getId())
                .empCode(emp.getEmployeeNo())
                .empName(emp.getPayrollName())
                .departmentName(emp.getDepartment() != null ? emp.getDepartment().getName() : null)
                .designationName(emp.getDesignation() != null ? emp.getDesignation().getName() : null)
                .branchName(emp.getBranch() != null ? emp.getBranch().getName() : null)
                .calculatedAmount(e.getAmount())
                .adjustedAmount(e.getAdjustedAmount())
                .effectiveAmount(eff)
                .formulaExpression(e.getFormulaExpression())
                .formulaResult(e.getFormulaResult())
                .status(e.getStatus())
                .approvedById(e.getApprovedBy() != null ? e.getApprovedBy().getId() : null)
                .approvedByUserName(e.getApprovedBy() != null ? e.getApprovedBy().getUserName() : null)
                .approvedDate(e.getApprovedDate())
                .note(e.getNote())
                .createdDate(e.getCreatedDate())
                .modifiedDate(e.getModifiedDate())
                .build();
    }
}
