package com.payroll.importexport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import com.payroll.dto.response.ImportCommitResponseDTO;
import com.payroll.dto.response.ImportFormatResponseDTO;
import com.payroll.dto.response.ImportLogResponseDTO;
import com.payroll.dto.response.ImportPreviewResponseDTO;
import com.payroll.dto.response.ImportRowDTO;
import com.payroll.dto.response.RowErrorDTO;
import com.payroll.entity.*;
import com.payroll.enums.GratuityStatus;
import com.payroll.enums.ImportEntityType;
import com.payroll.enums.ImportStatus;
import com.payroll.exception.ImportException;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Coordinates the import pipeline: parse → validate → stage session →
 * commit (bulk save + import_log) — plus blank template generation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImportOrchestrator {

    static final int SESSION_TTL_MINUTES = 30;

    /** Normalised header aliases → expected field names. */
    private static final Map<String, String> HEADER_ALIASES = Map.ofEntries(
            Map.entry("empcode", "empCode"),
            Map.entry("employeecode", "empCode"),
            Map.entry("employeeno", "empCode"),
            Map.entry("empno", "empCode"),
            Map.entry("nopaycode", "nopayCode"),
            Map.entry("nopay", "nopayCode"),
            Map.entry("otcode", "otCode"),
            Map.entry("overtimecode", "otCode"),
            Map.entry("overtime", "otCode"),
            Map.entry("bonuscode", "bonusCode"),
            Map.entry("bonus", "bonusCode"),
            Map.entry("facode", "faCode"),
            Map.entry("fixedallowance", "faCode"),
            Map.entry("fdcode", "fdCode"),
            Map.entry("fixeddeduction", "fdCode"),
            Map.entry("loancode", "loanCode"),
            Map.entry("loan", "loanCode"),
            Map.entry("vacode", "vaCode"),
            Map.entry("variableallowance", "vaCode"),
            Map.entry("vdcode", "vdCode"),
            Map.entry("variablededuction", "vdCode"),
            Map.entry("days", "days"),
            Map.entry("nopaydays", "days"),
            Map.entry("hours", "hours"),
            Map.entry("amount", "amount"),
            Map.entry("payrollmonth", "payrollMonth"),
            Map.entry("month", "payrollMonth"),
            Map.entry("terminationdate", "terminationDate"),
            Map.entry("joineddate", "joinedDate"),
            Map.entry("yearsofservice", "yearsOfService"),
            Map.entry("basicsalary", "basicSalary"),
            Map.entry("gratuityamount", "gratuityAmount"),
            Map.entry("remarks", "remarks"));

    private static final TypeReference<List<Map<String, String>>> ROWS_TYPE =
            new TypeReference<>() {};
    private static final TypeReference<Map<String, String>> MAPPING_TYPE =
            new TypeReference<>() {};

    private final List<FileParserStrategy> parsers;
    private final ImportValidator validator;
    private final AuditUserResolver auditUserResolver;
    private final ObjectMapper objectMapper;

    private final ImportSessionRepository sessionRepository;
    private final ImportLogRepository importLogRepository;
    private final EmployeeRepository employeeRepository;
    private final NopayRepository nopayRepository;
    private final OvertimeRepository overtimeRepository;
    private final BonusRepository bonusRepository;
    private final FixedAllowanceRepository fixedAllowanceRepository;
    private final FixedDeductionRepository fixedDeductionRepository;
    private final LoanRepository loanRepository;
    private final VariableAllowanceRepository variableAllowanceRepository;
    private final VariableDeductionRepository variableDeductionRepository;
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

    // ── Upload ───────────────────────────────────────────────────────────

    @Transactional
    public ImportPreviewResponseDTO upload(MultipartFile file, ImportEntityType entity,
                                           String payrollMonth, Long fallbackUserId) {
        requireValidMonth(payrollMonth);
        if (file == null || file.isEmpty()) {
            throw new ImportException("No file uploaded");
        }
        Usr user = auditUserResolver.resolve(fallbackUserId);

        FileParserStrategy parser = parsers.stream()
                .filter(p -> p.supports(file.getOriginalFilename()))
                .findFirst()
                .orElseThrow(() -> new ImportException(
                        "Unsupported file type — upload .xlsx or .csv"));

        List<Map<String, String>> rawRows;
        try {
            rawRows = parser.parse(file.getInputStream());
        } catch (IOException e) {
            throw new ImportException("Could not read file: " + e.getMessage(), e);
        }
        if (rawRows.isEmpty()) {
            throw new ImportException("File contains no data rows");
        }

        sessionRepository.deleteAllByExpiresAtBefore(LocalDateTime.now());

        Map<String, String> mapping = autoMap(entity, rawRows.get(0).keySet());

        ImportSession session = ImportSession.builder()
                .id(UUID.randomUUID().toString())
                .entity(entity)
                .payrollMonth(payrollMonth)
                .fileName(file.getOriginalFilename())
                .rowsJson(toJson(rawRows))
                .mappingJson(toJson(mapping))
                .createdBy(user.getId())
                .expiresAt(LocalDateTime.now().plusMinutes(SESSION_TTL_MINUTES))
                .build();
        sessionRepository.save(session);

        return buildPreview(session, rawRows, mapping);
    }

    // ── Re-validate after column remapping ───────────────────────────────

    @Transactional
    public ImportPreviewResponseDTO revalidate(String sessionId, Map<String, String> mapping) {
        ImportSession session = loadSession(sessionId);
        List<Map<String, String>> rawRows = fromJson(session.getRowsJson(), ROWS_TYPE);

        session.setMappingJson(toJson(mapping));
        sessionRepository.save(session);

        return buildPreview(session, rawRows, mapping);
    }

    // ── Commit ───────────────────────────────────────────────────────────

    @Transactional
    public ImportCommitResponseDTO commit(String sessionId, Long fallbackUserId) {
        ImportSession session = loadSession(sessionId);
        Usr user = auditUserResolver.resolve(fallbackUserId);

        List<Map<String, String>> rawRows = fromJson(session.getRowsJson(), ROWS_TYPE);
        Map<String, String> mapping = session.getMappingJson() == null
                ? Map.of() : fromJson(session.getMappingJson(), MAPPING_TYPE);
        List<Map<String, String>> mappedRows = applyMapping(rawRows, mapping);

        ValidationResult result = validator.validate(session.getEntity(), mappedRows);
        if (result.getValidRows() == 0) {
            throw new ImportException("Nothing to commit — every row has validation errors");
        }

        ImportLog importLog = importLogRepository.save(ImportLog.builder()
                .entity(session.getEntity())
                .payrollMonth(session.getPayrollMonth())
                .fileName(session.getFileName())
                .totalRows(result.getTotalRows())
                .validRows(result.getValidRows())
                .errorRows(result.getErrorRows())
                .errorDetail(result.getErrors().isEmpty() ? null : toJson(result.getErrors()))
                .status(ImportStatus.COMMITTED)
                .createdBy(user)
                .build());

        List<ImportRowDTO> validRows = result.getRows().stream()
                .filter(ImportRowDTO::isValid)
                .toList();
        persistRows(session.getEntity(), session.getPayrollMonth(), validRows,
                importLog.getId(), user);

        sessionRepository.delete(session);
        log.info("Import {} committed: {} rows into {} for {}", importLog.getId(),
                validRows.size(), session.getEntity(), session.getPayrollMonth());

        return ImportCommitResponseDTO.builder()
                .importLogId(importLog.getId())
                .insertedRows(validRows.size())
                .skippedRows(result.getErrorRows())
                .build();
    }

    // ── Import log listing ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ImportLogResponseDTO> getImportLogs() {
        return importLogRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(this::toLogDto)
                .toList();
    }

    public ImportLogResponseDTO toLogDto(ImportLog logEntry) {
        List<RowErrorDTO> detail = logEntry.getErrorDetail() == null ? List.of()
                : fromJson(logEntry.getErrorDetail(), new TypeReference<List<RowErrorDTO>>() {});
        return ImportLogResponseDTO.builder()
                .id(logEntry.getId())
                .entity(logEntry.getEntity().name())
                .payrollMonth(logEntry.getPayrollMonth())
                .fileName(logEntry.getFileName())
                .totalRows(logEntry.getTotalRows())
                .validRows(logEntry.getValidRows())
                .errorRows(logEntry.getErrorRows())
                .status(logEntry.getStatus().name())
                .errorDetail(detail)
                .createdBy(logEntry.getCreatedBy().getUsername())
                .createdAt(logEntry.getCreatedAt())
                .build();
    }

    // ── Format metadata ──────────────────────────────────────────────────

    /** Describes the expected file layout for every importable entity. */
    public List<ImportFormatResponseDTO> getFormats() {
        return Arrays.stream(ImportEntityType.values())
                .map(e -> ImportFormatResponseDTO.builder()
                        .entity(e.name())
                        .expectedFields(e.getExpectedFields())
                        .requiredFields(e.getRequiredFields())
                        .keyFields(e.getKeyFields())
                        .numericFields(e.getNumericFields())
                        .supportedFormats(List.of("xlsx", "csv"))
                        .sampleRow(sampleRow(e))
                        .build())
                .toList();
    }

    // ── Blank template ───────────────────────────────────────────────────

    public byte[] buildTemplate(ImportEntityType entity, String format) {
        List<String> headers = entity.getExpectedFields();
        List<String> sample = sampleRow(entity);
        try {
            if ("csv".equalsIgnoreCase(format)) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try (CSVWriter writer = new CSVWriter(
                        new OutputStreamWriter(bos, StandardCharsets.UTF_8))) {
                    writer.writeNext(headers.toArray(String[]::new));
                    writer.writeNext(sample.toArray(String[]::new));
                }
                return bos.toByteArray();
            }
            try (XSSFWorkbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet(entity.name());
                Row headerRow = sheet.createRow(0);
                Row sampleRow = sheet.createRow(1);
                for (int c = 0; c < headers.size(); c++) {
                    headerRow.createCell(c).setCellValue(headers.get(c));
                    sampleRow.createCell(c).setCellValue(sample.get(c));
                }
                workbook.write(bos);
                return bos.toByteArray();
            }
        } catch (IOException e) {
            throw new ImportException("Could not build template: " + e.getMessage(), e);
        }
    }

    private static List<String> sampleRow(ImportEntityType entity) {
        return switch (entity) {
            case EMP_NOPAY -> List.of("EMP001", "NP01", "1.50", "2500.00", "2026-06");
            case EMP_OT -> List.of("EMP001", "OT01", "4.00", "3200.00", "2026-06");
            case EMP_LATE -> List.of("EMP001", "2.25", "1500.00", "2026-06");
            case EMP_SALARY_ADVANCE -> List.of("EMP001", "10000.00", "2026-06");
            case EMP_BONUS -> List.of("EMP001", "BN01", "15000.00", "2026-06");
            case EMP_FA -> List.of("EMP001", "FA01", "5000.00", "2026-06");
            case EMP_FD -> List.of("EMP001", "FD01", "1200.00", "2026-06");
            case EMP_LOAN -> List.of("EMP001", "LN01", "7500.00", "2026-06");
            case EMP_SAL_INCR -> List.of("EMP001", "6000.00", "2026-06");
            case EMP_VA -> List.of("EMP001", "VA01", "2500.00", "2026-06");
            case EMP_VD -> List.of("EMP001", "VD01", "800.00", "2026-06");
            case EMP_GRATUITY -> List.of("EMP001", "2026-05-31", "2019-01-15",
                    "7.40", "85000.00", "314500.00", "Resignation");
        };
    }

    // ── Internals ────────────────────────────────────────────────────────

    private ImportPreviewResponseDTO buildPreview(ImportSession session,
                                                  List<Map<String, String>> rawRows,
                                                  Map<String, String> mapping) {
        List<Map<String, String>> mappedRows = applyMapping(rawRows, mapping);
        ValidationResult result = validator.validate(session.getEntity(), mappedRows);
        return ImportPreviewResponseDTO.builder()
                .sessionId(session.getId())
                .entity(session.getEntity().name())
                .payrollMonth(session.getPayrollMonth())
                .fileName(session.getFileName())
                .detectedHeaders(List.copyOf(rawRows.get(0).keySet()))
                .expectedFields(session.getEntity().getExpectedFields())
                .mapping(mapping)
                .totalRows(result.getTotalRows())
                .validRows(result.getValidRows())
                .errorRows(result.getErrorRows())
                .rows(result.getRows())
                .build();
    }

    private ImportSession loadSession(String sessionId) {
        ImportSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Import session not found or expired: " + sessionId));
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            sessionRepository.delete(session);
            throw new ImportException("Import session expired — please upload the file again");
        }
        return session;
    }

    /** Auto-detect mapping: expected field → file header, via normalised aliases. */
    private static Map<String, String> autoMap(ImportEntityType entity, Set<String> headers) {
        Map<String, String> mapping = new LinkedHashMap<>();
        for (String header : headers) {
            String normalised = header.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
            String field = HEADER_ALIASES.get(normalised);
            if (field != null && entity.getExpectedFields().contains(field)) {
                mapping.putIfAbsent(field, header);
            }
        }
        return mapping;
    }

    private static List<Map<String, String>> applyMapping(List<Map<String, String>> rawRows,
                                                          Map<String, String> mapping) {
        List<Map<String, String>> mapped = new ArrayList<>(rawRows.size());
        for (Map<String, String> raw : rawRows) {
            Map<String, String> row = new LinkedHashMap<>();
            mapping.forEach((field, header) -> row.put(field, raw.get(header)));
            mapped.add(row);
        }
        return mapped;
    }

    private void persistRows(ImportEntityType entity, String payrollMonth,
                             List<ImportRowDTO> rows, Long importLogId, Usr user) {
        Map<String, Employee> empCache = new HashMap<>();
        Map<String, Nopay> nopayCache = new HashMap<>();
        Map<String, Overtime> otCache = new HashMap<>();
        Map<String, Bonus> bonusCache = new HashMap<>();
        Map<String, FixedAllowance> faCache = new HashMap<>();
        Map<String, FixedDeduction> fdCache = new HashMap<>();
        Map<String, Loan> loanCache = new HashMap<>();
        Map<String, VariableAllowance> vaCache = new HashMap<>();
        Map<String, VariableDeduction> vdCache = new HashMap<>();

        switch (entity) {
            case EMP_NOPAY -> employeeNopayRepository.saveAll(rows.stream()
                    .map(r -> EmployeeNopay.builder()
                            .employee(employee(r, empCache))
                            .nopay(nopay(r, nopayCache))
                            .days(decimal(r, "days"))
                            .amount(decimal(r, "amount"))
                            .payrollMonth(payrollMonth)
                            .isProcessed(false)
                            .createdBy(user)
                            .modifiedBy(user)
                            .importLogId(importLogId)
                            .build())
                    .toList());
            case EMP_OT -> employeeOvertimeRepository.saveAll(rows.stream()
                    .map(r -> EmployeeOvertime.builder()
                            .employee(employee(r, empCache))
                            .overtime(overtime(r, otCache))
                            .hours(decimal(r, "hours"))
                            .amount(decimal(r, "amount"))
                            .payrollMonth(payrollMonth)
                            .isProcessed(false)
                            .createdBy(user)
                            .modifiedBy(user)
                            .importLogId(importLogId)
                            .build())
                    .toList());
            case EMP_LATE -> employeeLateRepository.saveAll(rows.stream()
                    .map(r -> EmployeeLate.builder()
                            .employee(employee(r, empCache))
                            .hours(decimal(r, "hours"))
                            .amount(decimal(r, "amount"))
                            .payrollMonth(payrollMonth)
                            .isProcessed(false)
                            .createdBy(user)
                            .modifiedBy(user)
                            .importLogId(importLogId)
                            .build())
                    .toList());
            case EMP_SALARY_ADVANCE -> employeeSalaryAdvanceRepository.saveAll(rows.stream()
                    .map(r -> EmployeeSalaryAdvance.builder()
                            .employee(employee(r, empCache))
                            .amount(decimal(r, "amount"))
                            .payrollMonth(payrollMonth)
                            .isProcessed(false)
                            .createdBy(user)
                            .modifiedBy(user)
                            .importLogId(importLogId)
                            .build())
                    .toList());
            case EMP_BONUS -> employeeBonusRepository.saveAll(rows.stream()
                    .map(r -> EmployeeBonus.builder()
                            .employee(employee(r, empCache))
                            .bonus(resolve(r, "bonusCode", bonusCache,
                                    bonusRepository::findByCodeIgnoreCase, "bonus"))
                            .amount(decimal(r, "amount"))
                            .payrollMonth(payrollMonth)
                            .isProcessed(false)
                            .createdBy(user)
                            .modifiedBy(user)
                            .importLogId(importLogId)
                            .build())
                    .toList());
            case EMP_FA -> employeeFixedAllowanceRepository.saveAll(rows.stream()
                    .map(r -> EmployeeFixedAllowance.builder()
                            .employee(employee(r, empCache))
                            .fixedAllowance(resolve(r, "faCode", faCache,
                                    fixedAllowanceRepository::findByCodeIgnoreCase, "fixed allowance"))
                            .amount(decimal(r, "amount"))
                            .payrollMonth(payrollMonth)
                            .isProcessed(false)
                            .createdBy(user)
                            .modifiedBy(user)
                            .importLogId(importLogId)
                            .build())
                    .toList());
            case EMP_FD -> employeeFixedDeductionRepository.saveAll(rows.stream()
                    .map(r -> EmployeeFixedDeduction.builder()
                            .employee(employee(r, empCache))
                            .fixedDeduction(resolve(r, "fdCode", fdCache,
                                    fixedDeductionRepository::findByCodeIgnoreCase, "fixed deduction"))
                            .amount(decimal(r, "amount"))
                            .payrollMonth(payrollMonth)
                            .isProcessed(false)
                            .createdBy(user)
                            .modifiedBy(user)
                            .importLogId(importLogId)
                            .build())
                    .toList());
            case EMP_LOAN -> employeeLoanRepository.saveAll(rows.stream()
                    .map(r -> EmployeeLoan.builder()
                            .employee(employee(r, empCache))
                            .loan(resolve(r, "loanCode", loanCache,
                                    loanRepository::findByCodeIgnoreCase, "loan"))
                            .amount(decimal(r, "amount"))
                            .payrollMonth(payrollMonth)
                            .isProcessed(false)
                            .createdBy(user)
                            .modifiedBy(user)
                            .importLogId(importLogId)
                            .build())
                    .toList());
            case EMP_SAL_INCR -> employeeSalaryIncrementRepository.saveAll(rows.stream()
                    .map(r -> EmployeeSalaryIncrement.builder()
                            .employee(employee(r, empCache))
                            .amount(decimal(r, "amount"))
                            .payrollMonth(payrollMonth)
                            .isProcessed(false)
                            .createdBy(user)
                            .modifiedBy(user)
                            .importLogId(importLogId)
                            .build())
                    .toList());
            case EMP_VA -> employeeVariableAllowanceRepository.saveAll(rows.stream()
                    .map(r -> EmployeeVariableAllowance.builder()
                            .employee(employee(r, empCache))
                            .variableAllowance(resolve(r, "vaCode", vaCache,
                                    variableAllowanceRepository::findByCodeIgnoreCase, "variable allowance"))
                            .amount(decimal(r, "amount"))
                            .payrollMonth(payrollMonth)
                            .isProcessed(false)
                            .createdBy(user)
                            .modifiedBy(user)
                            .importLogId(importLogId)
                            .build())
                    .toList());
            case EMP_VD -> employeeVariableDeductionRepository.saveAll(rows.stream()
                    .map(r -> EmployeeVariableDeduction.builder()
                            .employee(employee(r, empCache))
                            .variableDeduction(resolve(r, "vdCode", vdCache,
                                    variableDeductionRepository::findByCodeIgnoreCase, "variable deduction"))
                            .amount(decimal(r, "amount"))
                            .payrollMonth(payrollMonth)
                            .isProcessed(false)
                            .createdBy(user)
                            .modifiedBy(user)
                            .importLogId(importLogId)
                            .build())
                    .toList());
            case EMP_GRATUITY -> persistGratuities(rows, importLogId, user, empCache);
        }
    }

    /**
     * Gratuity rows: unique code generated like the existing service
     * ({@code GT-%05d}), joinedDate falls back to the employee's joined date,
     * status starts at DRAFT.
     */
    private void persistGratuities(List<ImportRowDTO> rows, Long importLogId, Usr user,
                                   Map<String, Employee> empCache) {
        long seq = empGratuityRepository.count();
        List<EmpGratuity> gratuities = new ArrayList<>(rows.size());
        for (ImportRowDTO r : rows) {
            Employee emp = employee(r, empCache);
            String joinedRaw = r.getValues().get("joinedDate");
            LocalDate joined = joinedRaw == null || joinedRaw.isBlank()
                    ? emp.getJoinedDate()
                    : ImportValidator.parseDate(joinedRaw);
            String code;
            do {
                code = String.format("GT-%05d", ++seq);
            } while (empGratuityRepository.countByCode(code) > 0);
            String remarks = r.getValues().get("remarks");
            gratuities.add(EmpGratuity.builder()
                    .code(code)
                    .employee(emp)
                    .terminationDate(ImportValidator.parseDate(
                            r.getValues().get("terminationDate")))
                    .joinedDate(joined)
                    .yearsOfService(decimal(r, "yearsOfService"))
                    .basicSalary(decimal(r, "basicSalary"))
                    .gratuityAmount(decimal(r, "gratuityAmount"))
                    .status(GratuityStatus.DRAFT)
                    .remarks(remarks == null || remarks.isBlank() ? null : remarks)
                    .createdBy(user)
                    .modifiedBy(user)
                    .importLogId(importLogId)
                    .build());
        }
        empGratuityRepository.saveAll(gratuities);
    }

    /** Resolves a master-data row by code, caching per commit. */
    private <M> M resolve(ImportRowDTO row, String field, Map<String, M> cache,
                          java.util.function.Function<String, Optional<M>> lookup,
                          String what) {
        String code = row.getValues().get(field);
        return cache.computeIfAbsent(code.toLowerCase(Locale.ROOT),
                k -> lookup.apply(code).orElseThrow(
                        () -> new ImportException("Unknown " + what + " code: " + code)));
    }

    private Employee employee(ImportRowDTO row, Map<String, Employee> cache) {
        String code = row.getValues().get("empCode");
        return cache.computeIfAbsent(code.toLowerCase(Locale.ROOT),
                k -> employeeRepository.findByEmployeeNoIgnoreCase(code)
                        .orElseThrow(() -> new ImportException("Unknown employee: " + code)));
    }

    private Nopay nopay(ImportRowDTO row, Map<String, Nopay> cache) {
        String code = row.getValues().get("nopayCode");
        return cache.computeIfAbsent(code.toLowerCase(Locale.ROOT),
                k -> nopayRepository.findByCodeIgnoreCase(code)
                        .orElseThrow(() -> new ImportException("Unknown no-pay code: " + code)));
    }

    private Overtime overtime(ImportRowDTO row, Map<String, Overtime> cache) {
        String code = row.getValues().get("otCode");
        return cache.computeIfAbsent(code.toLowerCase(Locale.ROOT),
                k -> overtimeRepository.findByCodeIgnoreCase(code)
                        .orElseThrow(() -> new ImportException("Unknown overtime code: " + code)));
    }

    private static BigDecimal decimal(ImportRowDTO row, String field) {
        return new BigDecimal(row.getValues().get(field).replace(",", "").trim());
    }

    static void requireValidMonth(String payrollMonth) {
        if (payrollMonth == null || !ImportValidator.PAYROLL_MONTH.matcher(payrollMonth).matches()) {
            throw new ImportException("payrollMonth must match YYYY-MM");
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ImportException("Could not serialise import data", e);
        }
    }

    private <T> T fromJson(String json, TypeReference<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new ImportException("Could not read staged import data", e);
        }
    }
}
