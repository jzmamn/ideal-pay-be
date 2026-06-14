package com.payroll.formula;

import com.payroll.entity.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the standard MVEL variable context for payroll formula evaluation.
 *
 * <p>The context map is what gets passed directly into MVEL — every key
 * becomes a variable name accessible inside formula expressions.
 *
 * <p>Usage:
 * <pre>{@code
 * Map<String, Object> ctx = PayrollContextBuilder.builder()
 *     .employee(employee)
 *     .workingDays(26)
 *     .nopayDays(2)
 *     .otHours(BigDecimal.valueOf(10))
 *     .otRate(BigDecimal.valueOf(1.5))
 *     .fixedAllowances(empFaList)
 *     .variableAllowances(empVaList)
 *     .fixedDeductions(empFdList)
 *     .variableDeductions(empVdList)
 *     .nopayEntries(empNpList)
 *     .overtimeEntries(empOtList)
 *     .build();
 * }</pre>
 *
 * <p>Allowance / deduction variable naming convention (Option A):
 * <ul>
 *   <li>FixedAllowance code {@code FA_1}      → MVEL variable {@code FA_1}      (amount)</li>
 *   <li>VariableAllowance code {@code VA_2}   → MVEL variable {@code VA_2}      (amount)</li>
 *   <li>FixedDeduction code {@code FD_1}      → MVEL variable {@code FD_1}      (amount)</li>
 *   <li>VariableDeduction code {@code VD_3}   → MVEL variable {@code VD_3}      (amount)</li>
 *   <li>NopayDays code {@code NP_1}           → MVEL variables {@code NP_1}     (days),
 *                                                               {@code NP_1_amount} (computed amount)</li>
 *   <li>Overtime code {@code OT_1}            → MVEL variables {@code OT_1}     (hours),
 *                                                               {@code OT_1_amount} (computed amount)</li>
 * </ul>
 */
public class PayrollContextBuilder {

    private final Map<String, Object> context = new HashMap<>();

    private PayrollContextBuilder() {}

    public static PayrollContextBuilder builder() {
        return new PayrollContextBuilder();
    }

    // ── Employee ─────────────────────────────────────────────────────────────

    /**
     * Adds all standard employee-level variables:
     * {@code basicSalary}, {@code employeeNo}, {@code employeeId}.
     */
    public PayrollContextBuilder employee(Employee employee) {
        if (employee != null) {
            BigDecimal bs = employee.getBasicSalary() != null ? employee.getBasicSalary() : BigDecimal.ZERO;
            context.put("basicSalary",  bs);
            context.put("BASIC_SALARY", bs);
            context.put("employeeId",   employee.getId());
            context.put("employeeNo",   employee.getEmployeeNo());
        }
        return this;
    }

    /** Sets {@code basicSalary} directly (use when employee object is not available). */
    public PayrollContextBuilder basicSalary(BigDecimal basicSalary) {
        BigDecimal bs = basicSalary != null ? basicSalary : BigDecimal.ZERO;
        context.put("basicSalary",  bs);
        context.put("BASIC_SALARY", bs);
        return this;
    }

    // ── Pay period ────────────────────────────────────────────────────────────

    /** Total working/payable days in the period (e.g. 26). */
    public PayrollContextBuilder workingDays(int workingDays) {
        context.put("workingDays",  workingDays);
        context.put("WORKING_DAYS", workingDays);
        return this;
    }

    /** Number of no-pay (unpaid leave) days taken. */
    public PayrollContextBuilder nopayDays(int nopayDays) {
        context.put("nopayDays",  nopayDays);
        context.put("NOPAY_DAYS", nopayDays);
        return this;
    }

    /** Overtime hours worked during the period. */
    public PayrollContextBuilder otHours(BigDecimal otHours) {
        BigDecimal v = otHours != null ? otHours : BigDecimal.ZERO;
        context.put("otHours",  v);
        context.put("OT_HOURS", v);
        return this;
    }

    /** Overtime rate multiplier (e.g. 1.5 = time-and-a-half). */
    public PayrollContextBuilder otRate(BigDecimal otRate) {
        BigDecimal v = otRate != null ? otRate : BigDecimal.ONE;
        context.put("otRate",  v);
        context.put("OT_RATE", v);
        return this;
    }

    /** Total late hours (used in formula test / calculate endpoints). */
    public PayrollContextBuilder lateHours(BigDecimal lateHours) {
        BigDecimal v = lateHours != null ? lateHours : BigDecimal.ZERO;
        context.put("lateHours",  v);
        context.put("LATE_HOURS", v);
        return this;
    }

    // ── Allowances ────────────────────────────────────────────────────────────

    /**
     * Injects each fixed allowance into the context using its code as the variable name.
     * <pre>FA_1 = amount, FA_2 = amount, ...</pre>
     */
    public PayrollContextBuilder fixedAllowances(List<EmployeeFixedAllowance> list) {
        if (list != null) {
            list.forEach(efa -> context.put(
                    efa.getFixedAllowance().getCode(),
                    efa.getAmount() != null ? efa.getAmount() : BigDecimal.ZERO));
        }
        return this;
    }

    /**
     * Injects each variable allowance into the context using its code as the variable name.
     * <pre>VA_1 = amount, VA_2 = amount, ...</pre>
     */
    public PayrollContextBuilder variableAllowances(List<EmployeeVariableAllowance> list) {
        if (list != null) {
            list.forEach(eva -> context.put(
                    eva.getVariableAllowance().getCode(),
                    eva.getAmount() != null ? eva.getAmount() : BigDecimal.ZERO));
        }
        return this;
    }

    // ── Deductions ────────────────────────────────────────────────────────────

    /**
     * Injects each fixed deduction into the context using its code as the variable name.
     * <pre>FD_1 = amount, FD_2 = amount, ...</pre>
     */
    public PayrollContextBuilder fixedDeductions(List<EmployeeFixedDeduction> list) {
        if (list != null) {
            list.forEach(efd -> context.put(
                    efd.getFixedDeduction().getCode(),
                    efd.getAmount() != null ? efd.getAmount() : BigDecimal.ZERO));
        }
        return this;
    }

    /**
     * Injects each variable deduction into the context using its code as the variable name.
     * <pre>VD_1 = amount, VD_2 = amount, ...</pre>
     */
    public PayrollContextBuilder variableDeductions(List<EmployeeVariableDeduction> list) {
        if (list != null) {
            list.forEach(evd -> context.put(
                    evd.getVariableDeduction().getCode(),
                    evd.getAmount() != null ? evd.getAmount() : BigDecimal.ZERO));
        }
        return this;
    }

    // ── Nopay ─────────────────────────────────────────────────────────────────

    /**
     * Injects each nopay entry using its code as the variable name.
     * <pre>
     * NP_1        = days
     * NP_1_amount = computed amount
     * </pre>
     */
    public PayrollContextBuilder nopayEntries(List<EmployeeNopay> list) {
        if (list != null) {
            list.forEach(enp -> {
                String code = enp.getNopayDays().getCode();
                context.put(code,                enp.getDays()   != null ? enp.getDays()   : BigDecimal.ZERO);
                context.put(code + "_amount",    enp.getAmount() != null ? enp.getAmount() : BigDecimal.ZERO);
            });
        }
        return this;
    }

    // ── Late ──────────────────────────────────────────────────────────────────

    /**
     * Injects total late hours into the context as {@code lateHours}.
     */
    public PayrollContextBuilder lateEntries(List<EmployeeLate> list) {
        if (list != null) {
            BigDecimal totalLateHours = list.stream()
                    .map(el -> el.getHours() != null ? el.getHours() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            context.put("lateHours", totalLateHours);
        }
        return this;
    }

    // ── Overtime ──────────────────────────────────────────────────────────────

    /**
     * Injects each overtime entry using its code as the variable name.
     * <pre>
     * OT_1        = hours
     * OT_1_amount = computed amount
     * </pre>
     */
    public PayrollContextBuilder overtimeEntries(List<EmployeeOvertime> list) {
        if (list != null) {
            list.forEach(eot -> {
                String code = eot.getOvertime().getCode();
                context.put(code,                eot.getHours()  != null ? eot.getHours()  : BigDecimal.ZERO);
                context.put(code + "_amount",    eot.getAmount() != null ? eot.getAmount() : BigDecimal.ZERO);
            });
        }
        return this;
    }

    // ── Custom variables ──────────────────────────────────────────────────────

    /**
     * Merges an arbitrary map of extra variables into the context.
     * Keys already set by other builder methods are overwritten by entries in this map.
     */
    public PayrollContextBuilder customVariables(Map<String, Object> vars) {
        if (vars != null) {
            context.putAll(vars);
        }
        return this;
    }

    // ── Build ─────────────────────────────────────────────────────────────────

    /** Returns the assembled context map ready for MVEL evaluation. */
    public Map<String, Object> build() {
        context.putIfAbsent("basicSalary",  BigDecimal.ZERO);
        context.putIfAbsent("BASIC_SALARY", BigDecimal.ZERO);
        context.putIfAbsent("workingDays",  26);
        context.putIfAbsent("WORKING_DAYS", 26);
        context.putIfAbsent("nopayDays",    0);
        context.putIfAbsent("NOPAY_DAYS",   0);
        context.putIfAbsent("otHours",      BigDecimal.ZERO);
        context.putIfAbsent("OT_HOURS",     BigDecimal.ZERO);
        context.putIfAbsent("otRate",       BigDecimal.ONE);
        context.putIfAbsent("OT_RATE",      BigDecimal.ONE);
        return new HashMap<>(context);
    }
}
