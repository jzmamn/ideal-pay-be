package com.payroll.importexport;

import com.payroll.dto.response.ImportLogResponseDTO;
import com.payroll.entity.ImportLog;
import com.payroll.enums.GratuityStatus;
import com.payroll.enums.ImportStatus;
import com.payroll.exception.ImportException;
import com.payroll.exception.ImportLockedException;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Undoes a committed import by deleting every row tagged with its
 * {@code import_log_id}. If any of those rows has already been processed by a
 * payroll run, the import is marked {@code LOCKED} and the rollback is
 * rejected with 409 Conflict.
 *
 * <p>Two separate transactions are used so the LOCKED status survives the
 * exception that signals the conflict.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImportRollbackService {

    private final TransactionTemplate transactionTemplate;
    private final ImportLogRepository importLogRepository;
    private final EmployeeNopayRepository employeeNopayRepository;
    private final EmployeeOvertimeRepository employeeOvertimeRepository;
    private final EmployeeLateRepository employeeLateRepository;
    private final EmployeeSalaryAdvanceRepository employeeSalaryAdvanceRepository;
    private final EmployeeBonusRepository employeeBonusRepository;
    private final EmployeeFixedAllowanceRepository employeeFixedAllowanceRepository;
    private final EmployeeFixedDeductionRepository employeeFixedDeductionRepository;
    private final EmployeeLoanRepository employeeLoanRepository;
    private final EmployeeSalaryIncrementRepository employeeSalaryIncrementRepository;
    private final EmployeeVariableAllowanceRepository employeeVariableAllowanceRepository;
    private final EmployeeVariableDeductionRepository employeeVariableDeductionRepository;
    private final EmpGratuityRepository empGratuityRepository;
    private final ImportOrchestrator orchestrator;

    public ImportLogResponseDTO rollback(Long importLogId) {
        // Phase 1 — check & possibly lock (commits even though we throw after)
        ImportStatus outcome = transactionTemplate.execute(tx -> {
            ImportLog logEntry = load(importLogId);
            if (logEntry.getStatus() != ImportStatus.COMMITTED) {
                throw new ImportException("Import " + importLogId + " is "
                        + logEntry.getStatus() + " and cannot be rolled back");
            }
            if (processedCount(logEntry) > 0) {
                logEntry.setStatus(ImportStatus.LOCKED);
                importLogRepository.save(logEntry);
                return ImportStatus.LOCKED;
            }
            return ImportStatus.COMMITTED;
        });

        if (outcome == ImportStatus.LOCKED) {
            throw new ImportLockedException("Cannot roll back import " + importLogId
                    + " — some rows were already processed by a payroll run");
        }

        // Phase 2 — delete the imported rows and mark the log rolled back.
        // The DTO is built inside the transaction so lazy associations
        // (created_by) are still initialisable.
        return transactionTemplate.execute(tx -> {
            ImportLog logEntry = load(importLogId);
            long deleted = deleteRows(logEntry);
            logEntry.setStatus(ImportStatus.ROLLED_BACK);
            importLogRepository.save(logEntry);
            log.info("Import {} rolled back: {} rows deleted from {}",
                    importLogId, deleted, logEntry.getEntity());
            return orchestrator.toLogDto(logEntry);
        });
    }

    private ImportLog load(Long importLogId) {
        return importLogRepository.findById(importLogId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Import log not found: " + importLogId));
    }

    /** Rows that can no longer be undone: processed by a payroll run, or (for gratuity) past DRAFT. */
    private long processedCount(ImportLog logEntry) {
        Long id = logEntry.getId();
        return switch (logEntry.getEntity()) {
            case EMP_NOPAY -> employeeNopayRepository.countByImportLogIdAndIsProcessedTrue(id);
            case EMP_OT -> employeeOvertimeRepository.countByImportLogIdAndIsProcessedTrue(id);
            case EMP_LATE -> employeeLateRepository.countByImportLogIdAndIsProcessedTrue(id);
            case EMP_SALARY_ADVANCE ->
                    employeeSalaryAdvanceRepository.countByImportLogIdAndIsProcessedTrue(id);
            case EMP_BONUS -> employeeBonusRepository.countByImportLogIdAndIsProcessedTrue(id);
            case EMP_FA -> employeeFixedAllowanceRepository.countByImportLogIdAndIsProcessedTrue(id);
            case EMP_FD -> employeeFixedDeductionRepository.countByImportLogIdAndIsProcessedTrue(id);
            case EMP_LOAN -> employeeLoanRepository.countByImportLogIdAndIsProcessedTrue(id);
            case EMP_SAL_INCR ->
                    employeeSalaryIncrementRepository.countByImportLogIdAndIsProcessedTrue(id);
            case EMP_VA ->
                    employeeVariableAllowanceRepository.countByImportLogIdAndIsProcessedTrue(id);
            case EMP_VD ->
                    employeeVariableDeductionRepository.countByImportLogIdAndIsProcessedTrue(id);
            case EMP_GRATUITY ->
                    empGratuityRepository.countByImportLogIdAndStatusNot(id, GratuityStatus.DRAFT);
        };
    }

    private long deleteRows(ImportLog logEntry) {
        Long id = logEntry.getId();
        return switch (logEntry.getEntity()) {
            case EMP_NOPAY -> {
                long n = employeeNopayRepository.countByImportLogId(id);
                employeeNopayRepository.deleteAllByImportLogId(id);
                yield n;
            }
            case EMP_OT -> {
                long n = employeeOvertimeRepository.countByImportLogId(id);
                employeeOvertimeRepository.deleteAllByImportLogId(id);
                yield n;
            }
            case EMP_LATE -> {
                long n = employeeLateRepository.countByImportLogId(id);
                employeeLateRepository.deleteAllByImportLogId(id);
                yield n;
            }
            case EMP_SALARY_ADVANCE -> {
                long n = employeeSalaryAdvanceRepository.countByImportLogId(id);
                employeeSalaryAdvanceRepository.deleteAllByImportLogId(id);
                yield n;
            }
            case EMP_BONUS -> {
                long n = employeeBonusRepository.countByImportLogId(id);
                employeeBonusRepository.deleteAllByImportLogId(id);
                yield n;
            }
            case EMP_FA -> {
                long n = employeeFixedAllowanceRepository.countByImportLogId(id);
                employeeFixedAllowanceRepository.deleteAllByImportLogId(id);
                yield n;
            }
            case EMP_FD -> {
                long n = employeeFixedDeductionRepository.countByImportLogId(id);
                employeeFixedDeductionRepository.deleteAllByImportLogId(id);
                yield n;
            }
            case EMP_LOAN -> {
                long n = employeeLoanRepository.countByImportLogId(id);
                employeeLoanRepository.deleteAllByImportLogId(id);
                yield n;
            }
            case EMP_SAL_INCR -> {
                long n = employeeSalaryIncrementRepository.countByImportLogId(id);
                employeeSalaryIncrementRepository.deleteAllByImportLogId(id);
                yield n;
            }
            case EMP_VA -> {
                long n = employeeVariableAllowanceRepository.countByImportLogId(id);
                employeeVariableAllowanceRepository.deleteAllByImportLogId(id);
                yield n;
            }
            case EMP_VD -> {
                long n = employeeVariableDeductionRepository.countByImportLogId(id);
                employeeVariableDeductionRepository.deleteAllByImportLogId(id);
                yield n;
            }
            case EMP_GRATUITY -> {
                long n = empGratuityRepository.countByImportLogId(id);
                empGratuityRepository.deleteAllByImportLogId(id);
                yield n;
            }
        };
    }
}
