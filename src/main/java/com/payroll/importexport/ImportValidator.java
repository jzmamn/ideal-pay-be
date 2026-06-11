package com.payroll.importexport;

import com.payroll.dto.response.ImportRowDTO;
import com.payroll.dto.response.RowErrorDTO;
import com.payroll.enums.ImportEntityType;
import com.payroll.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Per-entity row validation mirroring the existing {@code RequestDTO}
 * constraints: required fields, numeric ≥ 0 (salary advance &gt; 0),
 * ISO dates, {@code empCode} must resolve to an employee, master codes must
 * resolve to their master rows, {@code payrollMonth} must be {@code YYYY-MM},
 * and no duplicate rows within the same file.
 */
@Component
@RequiredArgsConstructor
public class ImportValidator {

    public static final Pattern PAYROLL_MONTH = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])$");

    private final EmployeeRepository employeeRepository;
    private final NopayDaysRepository nopayDaysRepository;
    private final OvertimeRepository overtimeRepository;
    private final BonusRepository bonusRepository;
    private final FixedAllowanceRepository fixedAllowanceRepository;
    private final FixedDeductionRepository fixedDeductionRepository;
    private final LoanRepository loanRepository;
    private final VariableAllowanceRepository variableAllowanceRepository;
    private final VariableDeductionRepository variableDeductionRepository;

    /**
     * @param mappedRows rows already keyed by the entity's expected fields;
     *                   row numbers start at 2 (row 1 is the file header)
     */
    public ValidationResult validate(ImportEntityType entity,
                                     List<Map<String, String>> mappedRows) {
        List<ImportRowDTO> rows = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();
        Map<String, Boolean> empCache = new HashMap<>();
        Map<String, Boolean> codeCache = new HashMap<>();

        int rowNum = 1;
        for (Map<String, String> values : mappedRows) {
            rowNum++;
            List<RowErrorDTO> errors = new ArrayList<>();

            for (String field : entity.getRequiredFields()) {
                if (isBlank(values.get(field))) {
                    errors.add(error(rowNum, field, friendly(field) + " is required"));
                }
            }

            for (String field : entity.getNumericFields()) {
                String raw = values.get(field);
                if (isBlank(raw)) {
                    continue; // required-check already reported it
                }
                BigDecimal parsed = parseDecimal(raw);
                if (parsed == null) {
                    errors.add(error(rowNum, field, friendly(field) + " must be a number"));
                } else if (entity == ImportEntityType.EMP_SALARY_ADVANCE
                        && "amount".equals(field)
                        && parsed.compareTo(BigDecimal.ZERO) <= 0) {
                    errors.add(error(rowNum, field, "Amount must be greater than zero"));
                } else if (parsed.compareTo(BigDecimal.ZERO) < 0) {
                    errors.add(error(rowNum, field, friendly(field) + " must be zero or greater"));
                }
            }

            for (String field : entity.getDateFields()) {
                String raw = values.get(field);
                if (!isBlank(raw) && parseDate(raw) == null) {
                    errors.add(error(rowNum, field,
                            friendly(field) + " must be a date in yyyy-MM-dd format"));
                }
            }

            String empCode = values.get("empCode");
            if (!isBlank(empCode) && !resolveEmployee(empCode, empCache)) {
                errors.add(error(rowNum, "empCode",
                        "No employee found with code '" + empCode + "'"));
            }

            String codeField = entity.masterCodeField();
            if (codeField != null) {
                String code = values.get(codeField);
                if (!isBlank(code) && !resolveMasterCode(entity, code, codeCache)) {
                    errors.add(error(rowNum, codeField,
                            "No " + friendly(codeField).toLowerCase(Locale.ROOT)
                                    + " found matching '" + code + "'"));
                }
            }

            String month = values.get("payrollMonth");
            if (!isBlank(month) && !PAYROLL_MONTH.matcher(month).matches()) {
                errors.add(error(rowNum, "payrollMonth",
                        "Payroll month must match YYYY-MM"));
            }

            String dupKey = entity.getKeyFields().stream()
                    .map(f -> normalize(values.get(f)))
                    .collect(Collectors.joining("|"));
            boolean keyComplete = entity.getKeyFields().stream()
                    .noneMatch(f -> isBlank(values.get(f)));
            if (keyComplete && !seenKeys.add(dupKey)) {
                errors.add(error(rowNum, entity.getKeyFields().get(0),
                        "Duplicate row — same " + String.join(" + ", entity.getKeyFields())
                                + " appears earlier in this file"));
            }

            rows.add(ImportRowDTO.builder()
                    .rowNum(rowNum)
                    .values(values)
                    .errors(errors)
                    .build());
        }
        return ValidationResult.builder().rows(rows).build();
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private boolean resolveEmployee(String empCode, Map<String, Boolean> cache) {
        return cache.computeIfAbsent(normalize(empCode),
                k -> employeeRepository.findByEmployeeNoIgnoreCase(empCode).isPresent());
    }

    private boolean resolveMasterCode(ImportEntityType entity, String code,
                                      Map<String, Boolean> cache) {
        return cache.computeIfAbsent(entity.name() + ":" + normalize(code),
                k -> switch (entity) {
                    case EMP_NOPAY -> nopayDaysRepository.findByCodeIgnoreCase(code).isPresent();
                    case EMP_OT -> overtimeRepository.findByCodeIgnoreCase(code).isPresent();
                    case EMP_BONUS -> bonusRepository.findByCodeIgnoreCase(code).isPresent();
                    case EMP_FA -> fixedAllowanceRepository.findByCodeIgnoreCase(code).isPresent();
                    case EMP_FD -> fixedDeductionRepository.findByCodeIgnoreCase(code).isPresent();
                    case EMP_LOAN -> loanRepository.findByCodeIgnoreCase(code).isPresent();
                    case EMP_VA -> variableAllowanceRepository.findByCodeIgnoreCase(code).isPresent();
                    case EMP_VD -> variableDeductionRepository.findByCodeIgnoreCase(code).isPresent();
                    default -> false;
                });
    }

    static LocalDate parseDate(String raw) {
        try {
            return LocalDate.parse(raw.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static BigDecimal parseDecimal(String raw) {
        try {
            return new BigDecimal(raw.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static RowErrorDTO error(int rowNum, String field, String message) {
        return RowErrorDTO.builder().rowNum(rowNum).field(field).message(message).build();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    private static String friendly(String field) {
        return switch (field) {
            case "empCode" -> "Employee code";
            case "nopayCode" -> "No-pay code";
            case "otCode" -> "Overtime code";
            case "bonusCode" -> "Bonus code";
            case "faCode" -> "Fixed allowance code";
            case "fdCode" -> "Fixed deduction code";
            case "loanCode" -> "Loan code";
            case "vaCode" -> "Variable allowance code";
            case "vdCode" -> "Variable deduction code";
            case "payrollMonth" -> "Payroll month";
            case "terminationDate" -> "Termination date";
            case "joinedDate" -> "Joined date";
            case "yearsOfService" -> "Years of service";
            case "basicSalary" -> "Basic salary";
            case "gratuityAmount" -> "Gratuity amount";
            default -> Character.toUpperCase(field.charAt(0)) + field.substring(1);
        };
    }
}
