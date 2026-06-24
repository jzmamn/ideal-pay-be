package com.payroll.importexport;

import com.opencsv.CSVWriter;
import com.payroll.entity.*;
import com.payroll.enums.ImportEntityType;
import com.payroll.exception.ImportException;
import com.payroll.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Streams payroll data to XLSX ({@link SXSSFWorkbook}, 100-row window) or CSV
 * without materialising the full dataset: rows come from a JPA stream and are
 * detached as soon as they are written.
 */
@Service
@RequiredArgsConstructor
public class ExportService {

    private static final int SXSSF_WINDOW = 100;

    private final EntityManager entityManager;
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

    /** Must run inside a transaction so the JPA stream stays open. */
    @Transactional(readOnly = true)
    public void export(ImportEntityType entity, String format, String month, OutputStream out) {
        if (entity != ImportEntityType.EMP_GRATUITY) {
            ImportOrchestrator.requireValidMonth(month); // gratuity has no payroll month
        }
        boolean csv = "csv".equalsIgnoreCase(format);
        if (!csv && !"xlsx".equalsIgnoreCase(format)) {
            throw new ImportException("format must be xlsx or csv");
        }

        switch (entity) {
            case EMP_NOPAY -> write(csv, out, entity, headers(entity),
                    employeeNopayRepository.streamAllByPayrollMonth(month),
                    n -> new String[]{
                            n.getEmployee().getEmployeeNo(),
                            n.getEmployee().getPayrollName(),
                            n.getNopay().getCode(),
                            text(n.getDays()),
                            text(n.getAmount()),
                            n.getPayrollMonth(),
                            yn(n.getIsProcessed())});
            case EMP_OT -> write(csv, out, entity, headers(entity),
                    employeeOvertimeRepository.streamAllByPayrollMonth(month),
                    o -> new String[]{
                            o.getEmployee().getEmployeeNo(),
                            o.getEmployee().getPayrollName(),
                            o.getOvertime().getCode(),
                            text(o.getHours()),
                            text(o.getAmount()),
                            o.getPayrollMonth(),
                            yn(o.getIsProcessed())});
            case EMP_LATE -> write(csv, out, entity, headers(entity),
                    employeeLateRepository.streamAllByPayrollMonth(month),
                    l -> new String[]{
                            l.getEmployee().getEmployeeNo(),
                            l.getEmployee().getPayrollName(),
                            text(l.getHours()),
                            text(l.getAmount()),
                            l.getPayrollMonth(),
                            yn(l.getIsProcessed())});
            case EMP_SALARY_ADVANCE -> write(csv, out, entity, headers(entity),
                    employeeSalaryAdvanceRepository.streamAllByPayrollMonth(month),
                    s -> new String[]{
                            s.getEmployee().getEmployeeNo(),
                            s.getEmployee().getPayrollName(),
                            text(s.getAmount()),
                            s.getPayrollMonth(),
                            yn(s.getIsProcessed())});
            case EMP_BONUS -> write(csv, out, entity, headers(entity),
                    employeeBonusRepository.streamAllByPayrollMonth(month),
                    b -> new String[]{
                            b.getEmployee().getEmployeeNo(),
                            b.getEmployee().getPayrollName(),
                            b.getBonus() == null ? "" : b.getBonus().getCode(),
                            text(b.getAmount()),
                            b.getPayrollMonth(),
                            yn(b.getIsProcessed())});
            case EMP_FA -> write(csv, out, entity, headers(entity),
                    employeeFixedAllowanceRepository.streamAllByPayrollMonth(month),
                    f -> new String[]{
                            f.getEmployee().getEmployeeNo(),
                            f.getEmployee().getPayrollName(),
                            f.getFixedAllowance().getCode(),
                            text(f.getAmount()),
                            f.getPayrollMonth(),
                            yn(f.getIsProcessed())});
            case EMP_FD -> write(csv, out, entity, headers(entity),
                    employeeFixedDeductionRepository.streamAllByPayrollMonth(month),
                    f -> new String[]{
                            f.getEmployee().getEmployeeNo(),
                            f.getEmployee().getPayrollName(),
                            f.getFixedDeduction().getCode(),
                            text(f.getAmount()),
                            f.getPayrollMonth(),
                            yn(f.getIsProcessed())});
            case EMP_LOAN -> write(csv, out, entity, headers(entity),
                    employeeLoanRepository.streamAllByPayrollMonth(month),
                    l -> new String[]{
                            l.getEmployee().getEmployeeNo(),
                            l.getEmployee().getPayrollName(),
                            l.getLoan().getCode(),
                            text(l.getAmount()),
                            l.getPayrollMonth(),
                            yn(l.getIsProcessed())});
            case EMP_SAL_INCR -> write(csv, out, entity, headers(entity),
                    employeeSalaryIncrementRepository.streamAllByPayrollMonth(month),
                    s -> new String[]{
                            s.getEmployee().getEmployeeNo(),
                            s.getEmployee().getPayrollName(),
                            text(s.getAmount()),
                            s.getPayrollMonth(),
                            yn(s.getIsProcessed())});
            case EMP_VA -> write(csv, out, entity, headers(entity),
                    employeeVariableAllowanceRepository.streamAllByPayrollMonth(month),
                    v -> new String[]{
                            v.getEmployee().getEmployeeNo(),
                            v.getEmployee().getPayrollName(),
                            v.getVariableAllowance().getCode(),
                            text(v.getAmount()),
                            v.getPayrollMonth(),
                            yn(v.getIsProcessed())});
            case EMP_VD -> write(csv, out, entity, headers(entity),
                    employeeVariableDeductionRepository.streamAllByPayrollMonth(month),
                    v -> new String[]{
                            v.getEmployee().getEmployeeNo(),
                            v.getEmployee().getPayrollName(),
                            v.getVariableDeduction().getCode(),
                            text(v.getAmount()),
                            v.getPayrollMonth(),
                            yn(v.getIsProcessed())});
            case EMP_GRATUITY -> write(csv, out, entity, headers(entity),
                    empGratuityRepository.streamAllForExport(),
                    g -> new String[]{
                            g.getCode(),
                            g.getEmployee().getEmployeeNo(),
                            g.getEmployee().getPayrollName(),
                            text(g.getTerminationDate()),
                            text(g.getJoinedDate()),
                            text(g.getYearsOfService()),
                            text(g.getBasicSalary()),
                            text(g.getGratuityAmount()),
                            g.getStatus() == null ? "" : g.getStatus().name(),
                            g.getRemarks() == null ? "" : g.getRemarks()});
        }
    }

    public static List<String> headers(ImportEntityType entity) {
        return switch (entity) {
            case EMP_NOPAY -> List.of("empCode", "empName", "nopayCode",
                    "days", "amount", "payrollMonth", "isProcessed");
            case EMP_OT -> List.of("empCode", "empName", "otCode",
                    "hours", "amount", "payrollMonth", "isProcessed");
            case EMP_LATE -> List.of("empCode", "empName",
                    "hours", "amount", "payrollMonth", "isProcessed");
            case EMP_SALARY_ADVANCE, EMP_SAL_INCR -> List.of("empCode", "empName",
                    "amount", "payrollMonth", "isProcessed");
            case EMP_BONUS -> List.of("empCode", "empName", "bonusCode",
                    "amount", "payrollMonth", "isProcessed");
            case EMP_FA -> List.of("empCode", "empName", "faCode",
                    "amount", "payrollMonth", "isProcessed");
            case EMP_FD -> List.of("empCode", "empName", "fdCode",
                    "amount", "payrollMonth", "isProcessed");
            case EMP_LOAN -> List.of("empCode", "empName", "loanCode",
                    "amount", "payrollMonth", "isProcessed");
            case EMP_VA -> List.of("empCode", "empName", "vaCode",
                    "amount", "payrollMonth", "isProcessed");
            case EMP_VD -> List.of("empCode", "empName", "vdCode",
                    "amount", "payrollMonth", "isProcessed");
            case EMP_GRATUITY -> List.of("code", "empCode", "empName",
                    "terminationDate", "joinedDate", "yearsOfService",
                    "basicSalary", "gratuityAmount", "status", "remarks");
        };
    }

    // ── Writers ──────────────────────────────────────────────────────────

    private <T> void write(boolean csv, OutputStream out, ImportEntityType entity,
                           List<String> headers, Stream<T> rows, Function<T, String[]> mapper) {
        try (rows) {
            if (csv) {
                writeCsv(out, headers, rows, mapper);
            } else {
                writeXlsx(out, entity, headers, rows, mapper);
            }
        } catch (IOException e) {
            throw new ImportException("Export failed: " + e.getMessage(), e);
        }
    }

    private <T> void writeCsv(OutputStream out, List<String> headers,
                              Stream<T> rows, Function<T, String[]> mapper) throws IOException {
        CSVWriter writer = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        writer.writeNext(headers.toArray(String[]::new));
        rows.forEach(row -> {
            writer.writeNext(mapper.apply(row));
            entityManager.detach(row);
        });
        writer.flush(); // do not close — the servlet owns the stream
    }

    private <T> void writeXlsx(OutputStream out, ImportEntityType entity, List<String> headers,
                               Stream<T> rows, Function<T, String[]> mapper) throws IOException {
        SXSSFWorkbook workbook = new SXSSFWorkbook(SXSSF_WINDOW);
        try {
            SXSSFSheet sheet = workbook.createSheet(entity.name());
            Row headerRow = sheet.createRow(0);
            for (int c = 0; c < headers.size(); c++) {
                headerRow.createCell(c).setCellValue(headers.get(c));
            }
            int[] rowNum = {1};
            rows.forEach(row -> {
                String[] cells = mapper.apply(row);
                Row r = sheet.createRow(rowNum[0]++);
                for (int c = 0; c < cells.length; c++) {
                    r.createCell(c).setCellValue(cells[c] == null ? "" : cells[c]);
                }
                entityManager.detach(row);
            });
            workbook.write(out);
        } finally {
            workbook.dispose(); // remove SXSSF temp files
            workbook.close();
        }
    }

    private static String text(Object value) {
        return value == null ? "" : value.toString();
    }

    private static String yn(Boolean value) {
        return Boolean.TRUE.equals(value) ? "Y" : "N";
    }
}
