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
 *  Totals
 *    {{GROSS_PAY}}          {{TOTAL_DEDUCTIONS}}   {{NET_PAY}}
 *    {{EPF_EMPLOYEE}}       {{EPF_EMPLOYER}}       {{ETF}}
 *    {{PAYE_TAX}}
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

        // ── Totals ────────────────────────────────────────────────────────
        map.put("GROSS_PAY",        fmt(run.getGrossPay()));
        map.put("TOTAL_DEDUCTIONS", fmt(run.getTotalDeductions()));
        map.put("NET_PAY",          fmt(run.getNetPay()));
        map.put("EPF_EMPLOYEE",     fmt(run.getEmployeeEpf()));
        map.put("EPF_EMPLOYER",     fmt(run.getEmployerEpf()));
        map.put("ETF",              fmt(run.getEtf()));
        map.put("PAYE_TAX",         fmt(run.getPayeTax()));

        // ── Dynamic row fragments ─────────────────────────────────────────
        map.put("EARNINGS_ROWS",    buildEarningRows(run));
        map.put("DEDUCTIONS_ROWS",  buildDeductionRows(run));
        map.put("EMPLOYER_ROWS",    buildEmployerRows(run));

        return map;
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
