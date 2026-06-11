package com.payroll.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Payroll-data entities that support file import/export.
 * <p>
 * {@code expectedFields} are the logical column names an import file must map
 * to; fields listed in {@code keyFields} form the duplicate-detection key
 * within a single file.
 */
@Getter
public enum ImportEntityType {

    EMP_NOPAY(
            List.of("empCode", "nopayCode", "days", "amount", "payrollMonth"),
            List.of("empCode", "nopayCode", "days", "amount"),
            List.of("empCode", "nopayCode"),
            List.of("days", "amount"),
            List.of()),

    EMP_OT(
            List.of("empCode", "otCode", "hours", "amount", "payrollMonth"),
            List.of("empCode", "otCode", "hours", "amount"),
            List.of("empCode", "otCode"),
            List.of("hours", "amount"),
            List.of()),

    EMP_LATE(
            List.of("empCode", "hours", "amount", "payrollMonth"),
            List.of("empCode", "hours", "amount"),
            List.of("empCode"),
            List.of("hours", "amount"),
            List.of()),

    EMP_SALARY_ADVANCE(
            List.of("empCode", "amount", "payrollMonth"),
            List.of("empCode", "amount"),
            List.of("empCode"),
            List.of("amount"),
            List.of()),

    EMP_BONUS(
            List.of("empCode", "bonusCode", "amount", "payrollMonth"),
            List.of("empCode", "bonusCode", "amount"),
            List.of("empCode", "bonusCode"),
            List.of("amount"),
            List.of()),

    EMP_FA(
            List.of("empCode", "faCode", "amount", "payrollMonth"),
            List.of("empCode", "faCode", "amount"),
            List.of("empCode", "faCode"),
            List.of("amount"),
            List.of()),

    EMP_FD(
            List.of("empCode", "fdCode", "amount", "payrollMonth"),
            List.of("empCode", "fdCode", "amount"),
            List.of("empCode", "fdCode"),
            List.of("amount"),
            List.of()),

    EMP_LOAN(
            List.of("empCode", "loanCode", "amount", "payrollMonth"),
            List.of("empCode", "loanCode", "amount"),
            List.of("empCode", "loanCode"),
            List.of("amount"),
            List.of()),

    EMP_SAL_INCR(
            List.of("empCode", "amount", "payrollMonth"),
            List.of("empCode", "amount"),
            List.of("empCode"),
            List.of("amount"),
            List.of()),

    EMP_VA(
            List.of("empCode", "vaCode", "amount", "payrollMonth"),
            List.of("empCode", "vaCode", "amount"),
            List.of("empCode", "vaCode"),
            List.of("amount"),
            List.of()),

    EMP_VD(
            List.of("empCode", "vdCode", "amount", "payrollMonth"),
            List.of("empCode", "vdCode", "amount"),
            List.of("empCode", "vdCode"),
            List.of("amount"),
            List.of()),

    /**
     * Gratuity has no payroll month / processed flag: its unique {@code code}
     * is generated at commit, {@code joinedDate} falls back to the employee's
     * joined date, and rollback is blocked once a row leaves DRAFT.
     */
    EMP_GRATUITY(
            List.of("empCode", "terminationDate", "joinedDate", "yearsOfService",
                    "basicSalary", "gratuityAmount", "remarks"),
            List.of("empCode", "terminationDate", "yearsOfService",
                    "basicSalary", "gratuityAmount"),
            List.of("empCode", "terminationDate"),
            List.of("yearsOfService", "basicSalary", "gratuityAmount"),
            List.of("terminationDate", "joinedDate"));

    /** All logical fields, in template/column order. */
    private final List<String> expectedFields;
    /** Fields that must be present and non-blank on every row. */
    private final List<String> requiredFields;
    /** Fields forming the in-file duplicate key. */
    private final List<String> keyFields;
    /** Fields that must parse as a decimal number ≥ 0. */
    private final List<String> numericFields;
    /** Fields that must parse as an ISO date (yyyy-MM-dd). */
    private final List<String> dateFields;

    ImportEntityType(List<String> expectedFields, List<String> requiredFields,
                     List<String> keyFields, List<String> numericFields,
                     List<String> dateFields) {
        this.expectedFields = expectedFields;
        this.requiredFields = requiredFields;
        this.keyFields = keyFields;
        this.numericFields = numericFields;
        this.dateFields = dateFields;
    }

    /** The column holding this entity's master-data code, if it has one. */
    public String masterCodeField() {
        return switch (this) {
            case EMP_NOPAY -> "nopayCode";
            case EMP_OT -> "otCode";
            case EMP_BONUS -> "bonusCode";
            case EMP_FA -> "faCode";
            case EMP_FD -> "fdCode";
            case EMP_LOAN -> "loanCode";
            case EMP_VA -> "vaCode";
            case EMP_VD -> "vdCode";
            case EMP_LATE, EMP_SALARY_ADVANCE, EMP_SAL_INCR, EMP_GRATUITY -> null;
        };
    }

    public static ImportEntityType fromCode(String code) {
        return Arrays.stream(values())
                .filter(e -> e.name().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown import entity: " + code));
    }
}
