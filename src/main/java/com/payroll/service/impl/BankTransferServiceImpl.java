package com.payroll.service.impl;

import com.payroll.dto.request.BankTransferTemplateRequestDTO;
import com.payroll.dto.request.MarkTransferredRequestDTO;
import com.payroll.dto.response.BankTransferRowDTO;
import com.payroll.dto.response.BankTransferTemplateResponseDTO;
import com.payroll.entity.*;
import com.payroll.enums.ComponentType;
import com.payroll.enums.PayrollRunStatus;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.repository.*;
import com.payroll.service.BankTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BankTransferServiceImpl implements BankTransferService {

    private final BankTransferTemplateRepository templateRepository;
    private final EmpTransferLogRepository transferLogRepository;
    private final EmpPayrollRunRepository payrollRunRepository;
    private final BankRepository bankRepository;
    private final UsrRepository usrRepository;

    // ── Templates ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<BankTransferTemplateResponseDTO> getAllTemplates() {
        return templateRepository.findAllByOrderByBankNameAsc()
                .stream().map(this::toTemplateDTO).toList();
    }

    @Override
    public BankTransferTemplateResponseDTO saveTemplate(BankTransferTemplateRequestDTO req) {
        if (templateRepository.existsByBank_Id(req.getBankId())) {
            throw new IllegalArgumentException("A template for this bank already exists. Use update instead.");
        }
        Bank bank = bankRepository.findById(req.getBankId())
                .orElseThrow(() -> new ResourceNotFoundException("Bank", "id", req.getBankId()));
        Usr user = usrRepository.getReferenceById(req.getModifiedBy());

        BankTransferTemplate entity = BankTransferTemplate.builder()
                .bank(bank)
                .bankCode(req.getBankCode())
                .bankName(req.getBankName())
                .headerTemplate(req.getHeaderTemplate())
                .detailTemplate(req.getDetailTemplate())
                .footerTemplate(req.getFooterTemplate())
                .fileExtension(req.getFileExtension())
                .createdBy(user)
                .modifiedBy(user)
                .build();
        return toTemplateDTO(templateRepository.save(entity));
    }

    @Override
    public BankTransferTemplateResponseDTO updateTemplate(Long id, BankTransferTemplateRequestDTO req) {
        BankTransferTemplate existing = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BankTransferTemplate", "id", id));
        existing.setBankCode(req.getBankCode());
        existing.setBankName(req.getBankName());
        existing.setHeaderTemplate(req.getHeaderTemplate());
        existing.setDetailTemplate(req.getDetailTemplate());
        existing.setFooterTemplate(req.getFooterTemplate());
        existing.setFileExtension(req.getFileExtension());
        existing.setModifiedBy(usrRepository.getReferenceById(req.getModifiedBy()));
        return toTemplateDTO(templateRepository.save(existing));
    }

    @Override
    public void deleteTemplate(Long id) {
        BankTransferTemplate tmpl = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BankTransferTemplate", "id", id));
        templateRepository.delete(tmpl);
    }

    // ── Transfer preview ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<BankTransferRowDTO> getTransferPreview(String payrollMonth, List<String> types) {
        List<EmpPayrollRun> lockedRuns = payrollRunRepository
                .findAllByPayrollMonthAndStatus(payrollMonth, PayrollRunStatus.LOCKED, Sort.by("id"));

        // Collect run IDs already transferred for each requested type
        Set<Long> alreadyTransferred = new HashSet<>();
        for (String type : types) {
            transferLogRepository.findAllByPayrollRun_IdIn(
                    lockedRuns.stream().map(EmpPayrollRun::getId).toList()
            ).stream()
                    .filter(log -> log.getTransferType().equals(type))
                    .forEach(log -> alreadyTransferred.add(log.getPayrollRun().getId()));
        }

        return lockedRuns.stream()
                .map(run -> buildRow(run, types, alreadyTransferred))
                .toList();
    }

    // ── Mark transferred ──────────────────────────────────────────────────────

    @Override
    public void markTransferred(MarkTransferredRequestDTO req) {
        Usr transferredBy = usrRepository.getReferenceById(req.getTransferredBy());

        for (Long runId : req.getRunIds()) {
            EmpPayrollRun run = payrollRunRepository.findById(runId)
                    .orElseThrow(() -> new ResourceNotFoundException("EmpPayrollRun", "id", runId));

            Employee emp = run.getEmployee();
            Bank bank = emp.getBank();

            // One log entry per run (covers all types in the request together)
            EmpTransferLog log = EmpTransferLog.builder()
                    .payrollRun(run)
                    .transferType("SALARY")   // primary type; extend per-type if needed
                    .bank(bank)
                    .bankCode(bank != null ? bank.getCode() : null)
                    .transferredAmount(run.getNetPay())
                    .transferredBy(transferredBy)
                    .build();
            transferLogRepository.save(log);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BankTransferRowDTO buildRow(EmpPayrollRun run, List<String> types, Set<Long> alreadyTransferred) {
        Employee emp = run.getEmployee();
        Bank bank = emp.getBank();
        BankBranch branch = emp.getBankBranch();

        BigDecimal salaryAmt = run.getNetPay();
        BigDecimal advanceAmt = run.getDetails().stream()
                .filter(d -> ComponentType.SA.equals(d.getComponentType()))
                .map(EmpPayrollRunDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal faAmt = run.getDetails().stream()
                .filter(d -> ComponentType.FA.equals(d.getComponentType()))
                .map(EmpPayrollRunDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = BigDecimal.ZERO;
        if (types.contains("SALARY"))          total = total.add(salaryAmt);
        if (types.contains("SALARY_ADVANCE"))  total = total.add(advanceAmt);
        if (types.contains("FIXED_ALLOWANCE")) total = total.add(faAmt);

        boolean transferred = alreadyTransferred.contains(run.getId());

        return BankTransferRowDTO.builder()
                .runId(run.getId())
                .empId(emp.getId())
                .employeeNo(emp.getEmployeeNo())
                .empName(emp.getPayrollName())
                .bankId(bank != null ? bank.getId() : null)
                .bankCode(bank != null ? bank.getCode() : null)
                .bankName(bank != null ? bank.getName() : null)
                .branchCode(branch != null ? branch.getBranchCode() : null)
                .accountNo(emp.getAccountNo())
                .salaryAmount(salaryAmt)
                .salaryAdvanceAmount(advanceAmt)
                .fixedAllowanceAmount(faAmt)
                .totalAmount(total)
                .transferStatus(transferred ? "TRANSFERRED" : "PENDING")
                .transferredAt(null)
                .build();
    }

    private BankTransferTemplateResponseDTO toTemplateDTO(BankTransferTemplate e) {
        return BankTransferTemplateResponseDTO.builder()
                .id(e.getId())
                .bankId(e.getBank().getId())
                .bankCode(e.getBankCode())
                .bankName(e.getBankName())
                .headerTemplate(e.getHeaderTemplate())
                .detailTemplate(e.getDetailTemplate())
                .footerTemplate(e.getFooterTemplate())
                .fileExtension(e.getFileExtension())
                .build();
    }
}
