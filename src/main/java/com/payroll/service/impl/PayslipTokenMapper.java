package com.payroll.service.impl;

import com.payroll.entity.Company;
import com.payroll.entity.EmpPayrollRun;
import com.payroll.entity.EmpPayrollRunDetail;
import com.payroll.enums.ComponentType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Builds a flat {@code Map<String, String>} of {{TOKEN}} → value pairs
 * from a payroll run and company record.
 *
 * Token reference (use these inside your HTML template):
 *
 *  Company
 *    {{COMPANY_NAME}}       {{COMPANY_ADDRESS}}    {{COMPANY_PHONE}}
 *    {{COMPANY_EMAIL}}      {{COMPANY_LOGO}}       {{COMPANY_EPF_NO}}   {{COMPANY_ETF_NO}}
 *
 *  Employee
 *    {{EMPLOYEE_NO}}        {{EMPLOYEE_NAME}}      {{PAYROLL_NAME}}
 *    {{DESIGNATION}}        {{DEPARTMENT}}         {{EMPLOYEE_EPF_NO}}
 *    {{BANK_NAME}}          {{BANK_BRANCH}}        {{ACCOUNT_NO}}
 *
 *  Period
 *    {{PAYROLL_MONTH}}      {{WORKING_DAYS}}
 *
 *  Financials (aggregates)
 *    {{BASIC_SALARY}}
 *    {{GROSS_PAY}}          {{TOTAL_DEDUCTIONS}}   {{NET_PAY}}
 *    {{EPF_EMPLOYEE}}       {{EPF_EMPLOYER}}       {{ETF}}             {{PAYE_TAX}}
 *    {{NOPAY}}              {{LATE_DEDUCTION}}
 *    {{FIXED_ALLOWANCE}}    {{FIXED_DEDUCTION}}    {{OVERTIME}}
 *    {{VARIABLE_ALLOWANCE}} {{BONUS}}              {{INCREMENT}}       {{GRATUITY}}
 *    {{VARIABLE_DEDUCTION}}
 *
 *  Dynamic per-component tokens (resolved at runtime from component codes):
 *    {{FA_<CODE>}}  {{FD_<CODE>}}  {{OT_<CODE>}}  {{VA_<CODE>}}  {{VD_<CODE>}}
 *    e.g. {{FA_HRA}}, {{OT_OT1}}
 *
 *  Label tokens (static display names — use independently of value tokens):
 *    {{lblBasicSalary}}     {{lblGrossPay}}        {{lblNetPay}}
 *    {{lblTotalDeductions}} {{lblEpfEmployee}}      {{lblEpfEmployer}}
 *    {{lblEtf}}             {{lblPayeTax}}          {{lblNopay}}
 *    {{lblLateDeduction}}   {{lblFixedAllowance}}   {{lblFixedDeduction}}
 *    {{lblOvertime}}        {{lblVariableAllowance}} {{lblBonus}}
 *    {{lblIncrement}}       {{lblGratuity}}         {{lblVariableDeduction}}
 *    {{lblWorkingDays}}     {{lblPayrollMonth}}
 *    Per-component label: {{lbl_FA_<CODE>}}, {{lbl_FD_<CODE>}}, etc.
 *
 *  Dynamic rows (pre-built <tr> blocks — drop inside a <tbody>)
 *    {{EARNINGS_ROWS}}      {{DEDUCTIONS_ROWS}}    {{EMPLOYER_ROWS}}
 */
@Component
public class PayslipTokenMapper {

    private static final NumberFormat NUM;

    static {
        NUM = NumberFormat.getInstance(Locale.ENGLISH);
        NUM.setMinimumFractionDigits(2);
        NUM.setMaximumFractionDigits(2);
        NUM.setGroupingUsed(true);
    }

    public Map<String, String> buildTokens(EmpPayrollRun run, Company company) {
        Map<String, String> map = new LinkedHashMap<>();

        // ── Company ───────────────────────────────────────────────────────
        map.put("COMPANY_NAME",    nullSafe(company.getName()));
        map.put("COMPANY_ADDRESS", buildAddress(company));
        map.put("COMPANY_PHONE",   nullSafe(company.getTelephone()));
        map.put("COMPANY_EMAIL",   nullSafe(company.getEmail()));
        map.put("COMPANY_LOGO",    nullSafe(company.getLogo()));
        map.put("COMPANY_EPF_NO",  nullSafe(company.getEpfNo()));
        map.put("COMPANY_ETF_NO",  nullSafe(company.getEtfNo()));

        // ── Employee ──────────────────────────────────────────────────────
        var emp = run.getEmployee();
        map.put("EMPLOYEE_NO",      nullSafe(emp.getEmployeeNo()));
        map.put("EMPLOYEE_NAME",    emp.getFirstName() + " " + emp.getLastName());
        map.put("PAYROLL_NAME",     nullSafe(emp.getPayrollName()));
        map.put("DESIGNATION",      emp.getDesignation() != null ? emp.getDesignation().getName() : "");
        map.put("DEPARTMENT",       emp.getJobCategory() != null  ? emp.getJobCategory().getName()  : "");
        map.put("EMPLOYEE_EPF_NO",  nullSafe(emp.getEpfNo()));
        map.put("BANK_NAME",        emp.getBank()       != null   ? emp.getBank().getName()           : "");
        map.put("BANK_BRANCH",      emp.getBankBranch() != null   ? emp.getBankBranch().getBranchName() : "");
        map.put("ACCOUNT_NO",       nullSafe(emp.getAccountNo()));

        // ── Period ────────────────────────────────────────────────────────
        map.put("PAYROLL_MONTH",    formatMonth(run.getPayrollMonth()));
        map.put("WORKING_DAYS",     String.valueOf(run.getWorkingDays()));

        // ── Financials: header totals ─────────────────────────────────────
        map.put("BASIC_SALARY",      fmt(run.getBasicSalary()));
        map.put("GROSS_PAY",         fmt(run.getGrossPay()));
        map.put("TOTAL_DEDUCTIONS",  fmt(run.getTotalDeductions()));
        map.put("NET_PAY",           fmt(run.getNetPay()));
        map.put("EPF_EMPLOYEE",      fmt(run.getEmployeeEpf()));
        map.put("EPF_EMPLOYER",      fmt(run.getEmployerEpf()));
        map.put("ETF",               fmt(run.getEtf()));
        map.put("PAYE_TAX",          fmt(run.getPayeTax()));

        // ── Financials: detail aggregates ─────────────────────────────────
        buildDetailAggregates(run, map);

        // ── Dynamic per-component tokens (FA_CODE, lbl_FA_CODE, etc.) ────
        buildDynamicComponentTokens(run, map);

        // ── Static label tokens ───────────────────────────────────────────
        buildLabelTokens(map);

        // ── Dynamic row fragments ─────────────────────────────────────────
        map.put("EARNINGS_ROWS",     buildEarningRows(run));
        map.put("DEDUCTIONS_ROWS",   buildDeductionRows(run));
        map.put("EMPLOYER_ROWS",     buildEmployerRows(run));

        return map;
    }

    // ── Detail aggregates ─────────────────────────────────────────────────

    /**
     * Sums detail lines by ComponentType and well-known component codes to
     * populate aggregate tokens (NOPAY, FIXED_ALLOWANCE, BONUS, etc.).
     */
    private static void buildDetailAggregates(EmpPayrollRun run, Map<String, String> map) {
        BigDecimal nopay           = BigDecimal.ZERO;
        BigDecimal nopayDays       = BigDecimal.ZERO;
        BigDecimal lateDeduction   = BigDecimal.ZERO;
        BigDecimal fixedAllowance  = BigDecimal.ZERO;
        BigDecimal fixedDeduction  = BigDecimal.ZERO;
        BigDecimal overtime        = BigDecimal.ZERO;
        BigDecimal otHours         = BigDecimal.ZERO;
        BigDecimal variableAllow   = BigDecimal.ZERO;
        BigDecimal variableDeduct  = BigDecimal.ZERO;
        BigDecimal bonus           = BigDecimal.ZERO;
        BigDecimal increment       = BigDecimal.ZERO;
        BigDecimal gratuity        = BigDecimal.ZERO;

        for (EmpPayrollRunDetail d : run.getDetails()) {
            if (d.getComponentType() == null || d.getAmount() == null) continue;
            BigDecimal amt = d.getAmount();
            switch (d.getComponentType()) {
                case NOPAY -> {
                    nopay     = nopay.add(amt);
                    if (d.getDays() != null) nopayDays = nopayDays.add(d.getDays());
                }
                case LATE  -> lateDeduction  = lateDeduction.add(amt);
                case FA    -> fixedAllowance = fixedAllowance.add(amt);
                case FD    -> fixedDeduction = fixedDeduction.add(amt);
                case OT    -> {
                    overtime = overtime.add(amt);
                    if (d.getHours() != null) otHours = otHours.add(d.getHours());
                }
                case VD    -> variableDeduct = variableDeduct.add(amt);
                case VA    -> {
                    variableAllow = variableAllow.add(amt);
                    String code = d.getComponentCode() != null
                            ? d.getComponentCode().toUpperCase(Locale.ENGLISH) : "";
                    if (code.contains("BONUS"))     bonus     = bonus.add(amt);
                    if (code.contains("INCREMENT")) increment = increment.add(amt);
                    if (code.contains("GRATUITY"))  gratuity  = gratuity.add(amt);
                }
                default -> { /* EPF_EE, EPF_ER, ETF, PAYE, SA — handled elsewhere */ }
            }
        }

        map.put("NOPAY",             fmt(nopay));
        map.put("NOPAY_AMOUNT",      fmt(nopay));
        map.put("NOPAY_DAYS",        fmtDec(nopayDays));
        map.put("LATE_DEDUCTION",    fmt(lateDeduction));
        map.put("FIXED_ALLOWANCE",   fmt(fixedAllowance));
        map.put("FIXED_DEDUCTION",   fmt(fixedDeduction));
        map.put("OVERTIME",          fmt(overtime));
        map.put("OT_AMOUNT",         fmt(overtime));
        map.put("OT_HOURS",          fmtDec(otHours));
        map.put("VARIABLE_ALLOWANCE",fmt(variableAllow));
        map.put("VARIABLE_DEDUCTION",fmt(variableDeduct));
        map.put("BONUS",             fmt(bonus));
        map.put("INCREMENT",         fmt(increment));
        map.put("GRATUITY",          fmt(gratuity));
    }

    // ── Dynamic per-component tokens ──────────────────────────────────────

    /**
     * Per-component tokens using the code stored in the DB (already type-prefixed).
     *
     *  FA / FD / VA / VD:
     *    {{FA001}}          → amount
     *    {{lbl_FA001}}      → component name
     *
     *  OT:
     *    {{OT001}}          → amount
     *    {{lbl_OT001}}      → component name
     *    {{OT001_HOURS}}    → hours worked
     *    {{OT001_AMOUNT}}   → amount (alias)
     *
     *  NOPAY:
     *    {{NP001}}          → amount
     *    {{lbl_NP001}}      → component name
     *    {{NP001_DAYS}}     → no-pay days
     *    {{NP001_AMOUNT}}   → amount (alias)
     */
    private static void buildDynamicComponentTokens(EmpPayrollRun run, Map<String, String> map) {
        for (EmpPayrollRunDetail d : run.getDetails()) {
            ComponentType ct = d.getComponentType();
            if (ct == null) continue;
            boolean exposed = ct == ComponentType.FA || ct == ComponentType.FD
                    || ct == ComponentType.OT || ct == ComponentType.VA
                    || ct == ComponentType.VD || ct == ComponentType.NOPAY;
            if (!exposed) continue;

            String code = d.getComponentCode() != null
                    ? d.getComponentCode().toUpperCase(Locale.ENGLISH) : "";
            if (code.isBlank()) continue;

            map.put("lbl_" + code, escapeHtml(d.getComponentName()));

            if (ct == ComponentType.OT) {
                // {{OT001_HOURS}} {{OT001_AMOUNT}} — no bare {{OT001}}
                map.put(code + "_HOURS",  fmtDec(d.getHours()));
                map.put(code + "_AMOUNT", fmt(d.getAmount()));
            } else if (ct == ComponentType.NOPAY) {
                // {{NP001_DAYS}} {{NP001_AMOUNT}} — no bare {{NP001}}
                map.put(code + "_DAYS",   fmtDec(d.getDays()));
                map.put(code + "_AMOUNT", fmt(d.getAmount()));
            } else {
                // FA / FD / VA / VD — bare code is the amount e.g. {{FA001}}
                map.put(code, fmt(d.getAmount()));
            }
        }
    }

    // ── Static label tokens ───────────────────────────────────────────────

    /** Populates fixed human-readable labels for every data token. */
    private static void buildLabelTokens(Map<String, String> map) {
        map.put("lblBasicSalary",      "Basic Salary");
        map.put("lblGrossPay",         "Gross Pay");
        map.put("lblNetPay",           "Net Pay");
        map.put("lblTotalDeductions",  "Total Deductions");
        map.put("lblEpfEmployee",      "EPF (Employee 8%)");
        map.put("lblEpfEmployer",      "EPF (Employer 12%)");
        map.put("lblEtf",              "ETF (Employer 3%)");
        map.put("lblPayeTax",          "PAYE Tax");
        map.put("lblNopay",            "No-Pay");
        map.put("lblLateDeduction",    "Late Deduction");
        map.put("lblFixedAllowance",   "Fixed Allowance");
        map.put("lblFixedDeduction",   "Fixed Deduction");
        map.put("lblOvertime",         "Overtime");
        map.put("lblVariableAllowance","Variable Allowance");
        map.put("lblBonus",            "Bonus");
        map.put("lblIncrement",        "Increment");
        map.put("lblGratuity",         "Gratuity");
        map.put("lblVariableDeduction","Variable Deduction");
        map.put("lblWorkingDays",      "Working Days");
        map.put("lblPayrollMonth",     "Payroll Month");
        map.put("lblCompanyName",      "Company");
        map.put("lblEmployeeNo",       "Employee No");
        map.put("lblEmployeeName",     "Employee Name");
        map.put("lblDesignation",      "Designation");
        map.put("lblDepartment",       "Department");
        map.put("lblEpfNo",            "EPF No");
        map.put("lblBankName",         "Bank");
        map.put("lblBankBranch",       "Branch");
        map.put("lblAccountNo",        "Account No");
    }

    // ── Row builders ──────────────────────────────────────────────────────

    private String buildEarningRows(EmpPayrollRun run) {
        StringBuilder sb = new StringBuilder();

        // Basic salary is always first
        if (positive(run.getBasicSalary())) {
            sb.append(tr("Basic Salary", run.getBasicSalary()));
        }

        for (EmpPayrollRunDetail d : run.getDetails()) {
            if (d.getComponentType() == null) continue;
            if (isEarning(d.getComponentType()) && positive(d.getAmount())) {
                sb.append(tr(d.getComponentName(), d.getAmount()));
            }
        }
        return sb.toString();
    }

    private String buildDeductionRows(EmpPayrollRun run) {
        StringBuilder sb = new StringBuilder();

        for (EmpPayrollRunDetail d : run.getDetails()) {
            if (d.getComponentType() == null) continue;
            if (isDeduction(d.getComponentType()) && positive(d.getAmount())) {
                sb.append(tr(d.getComponentName(), d.getAmount()));
            }
        }

        // Statutory deductions (stored on the run header, not in details)
        if (positive(run.getEmployeeEpf())) sb.append(tr("EPF (Employee 8%)",  run.getEmployeeEpf()));
        if (positive(run.getPayeTax()))     sb.append(tr("PAYE Tax",            run.getPayeTax()));

        return sb.toString();
    }

    private String buildEmployerRows(EmpPayrollRun run) {
        StringBuilder sb = new StringBuilder();
        if (positive(run.getEmployerEpf())) sb.append(tr("EPF (Employer 12%)", run.getEmployerEpf()));
        if (positive(run.getEtf()))         sb.append(tr("ETF (Employer 3%)",  run.getEtf()));
        return sb.toString();
    }

    private static String tr(String label, BigDecimal amount) {
        return "<tr>"
             + "<td style=\"padding:0 0 2px; vertical-align:top;\">" + escapeHtml(label) + "</td>"
             + "<td style=\"padding:0 0 2px; text-align:right; white-space:nowrap; vertical-align:top;\">" + fmt(amount) + "</td>"
             + "</tr>\n";
    }

    // ── Classification ────────────────────────────────────────────────────

    private static boolean isEarning(ComponentType ct) {
        return ct == ComponentType.FA || ct == ComponentType.VA || ct == ComponentType.OT;
    }

    private static boolean isDeduction(ComponentType ct) {
        return ct == ComponentType.FD || ct == ComponentType.VD
            || ct == ComponentType.SA || ct == ComponentType.NOPAY
            || ct == ComponentType.LATE;
    }

    // ── Utilities ─────────────────────────────────────────────────────────

    private static String buildAddress(Company c) {
        StringBuilder addr = new StringBuilder();
        if (c.getAddressLine1() != null) addr.append(c.getAddressLine1());
        if (c.getAddressLine2() != null && !c.getAddressLine2().isBlank())
            addr.append(", ").append(c.getAddressLine2());
        if (c.getCity() != null) addr.append(", ").append(c.getCity());
        return addr.toString();
    }

    private static String formatMonth(String payrollMonth) {
        // payrollMonth format: "2026-05"
        if (payrollMonth == null || !payrollMonth.contains("-")) return nullSafe(payrollMonth);
        try {
            String[] parts = payrollMonth.split("-");
            String monthName = Month.of(Integer.parseInt(parts[1]))
                    .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            return monthName + " " + parts[0];
        } catch (Exception e) {
            return payrollMonth;
        }
    }

    private static String fmt(BigDecimal v) {
        return v == null ? "0.00" : NUM.format(v);
    }

    /** Formats a decimal quantity (days / hours) — strips trailing zeros, e.g. 1.50 → "1.5", 2.00 → "2". */
    private static String fmtDec(BigDecimal v) {
        if (v == null) return "0";
        return v.stripTrailingZeros().toPlainString();
    }

    private static boolean positive(BigDecimal v) {
        return v != null && v.compareTo(BigDecimal.ZERO) > 0;
    }

    private static String nullSafe(String s) {
        return s != null ? s : "";
    }

    /** Escapes component names to prevent stored-XSS if DB data is malicious. */
    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
