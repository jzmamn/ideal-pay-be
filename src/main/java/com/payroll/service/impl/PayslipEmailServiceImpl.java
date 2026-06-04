package com.payroll.service.impl;

import com.payroll.dto.request.PayslipEmailRequestDTO;
import com.payroll.dto.response.PayslipEmailResultDTO;
import com.payroll.entity.EmailConfig;
import com.payroll.entity.EmpPayrollRun;
import com.payroll.entity.EmpPayrollRunDetail;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.repository.EmailConfigRepository;
import com.payroll.repository.EmpPayrollRunRepository;
import com.payroll.service.PayslipEmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PayslipEmailServiceImpl implements PayslipEmailService {

    private final EmpPayrollRunRepository runRepository;
    private final EmailConfigRepository   emailConfigRepo;

    @Override
    public PayslipEmailResultDTO sendPayslips(PayslipEmailRequestDTO request) {

        EmailConfig cfg = emailConfigRepo.findTopByIsActiveTrueOrderByIdDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No email configuration found"));

        JavaMailSenderImpl sender = EmailConfigServiceImpl.buildSender(cfg);
        boolean landscape = "landscape".equalsIgnoreCase(request.getLayout());

        int sent = 0, failed = 0;
        List<String> errors = new ArrayList<>();

        for (Long runId : request.getRunIds()) {
            EmpPayrollRun run = runRepository.findById(runId)
                    .orElse(null);
            if (run == null) {
                errors.add("Run #" + runId + " not found");
                failed++;
                continue;
            }

            String toEmail = run.getEmployee().getEmail();
            if (toEmail == null || toEmail.isBlank()) {
                errors.add(run.getEmployee().getFirstName() + " " +
                        run.getEmployee().getLastName() + " has no email address");
                failed++;
                continue;
            }

            try {
                MimeMessage msg = sender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
                helper.setFrom(cfg.getFromAddress(), cfg.getFromName());
                helper.setTo(toEmail);
                helper.setSubject("Pay Slip — " + formatMonth(run.getPayrollMonth()));
                helper.setText(buildHtml(run, landscape), true);

                sender.send(msg);
                sent++;
            } catch (Exception ex) {
                log.error("Failed to send payslip to {} for run {}: {}", toEmail, runId, ex.getMessage());
                errors.add("Run #" + runId + ": " + ex.getMessage());
                failed++;
            }
        }

        return PayslipEmailResultDTO.builder()
                .sent(sent)
                .failed(failed)
                .errors(errors.isEmpty() ? null : errors)
                .build();
    }

    // ── HTML payslip builder ──────────────────────────────────────────────

    private String buildHtml(EmpPayrollRun run, boolean landscape) {
        String empName  = run.getEmployee().getFirstName() + " " + run.getEmployee().getLastName();
        String empCode  = run.getEmployee().getEmployeeNo();
        String period   = formatMonth(run.getPayrollMonth());
        String desgn    = run.getEmployee().getDesignation() != null
                ? run.getEmployee().getDesignation().getName() : "";

        // Split details into earnings / deductions
        StringBuilder earningsRows    = new StringBuilder();
        StringBuilder deductionsRows  = new StringBuilder();
        BigDecimal customDeductTotal  = BigDecimal.ZERO;

        for (EmpPayrollRunDetail d : run.getDetails()) {
            String type = d.getComponentType() != null ? d.getComponentType().name() : "";
            String row  = tr(d.getComponentName(), fmt(d.getAmount()));
            switch (type) {
                case "FIXED_ALLOWANCE", "VARIABLE_ALLOWANCE", "OVERTIME",
                     "BONUS", "INCREMENT", "GRATUITY" -> earningsRows.append(row);
                case "FIXED_DEDUCTION", "VARIABLE_DEDUCTION", "LOP",
                     "LOAN_EMI", "TAX", "DECREMENT"   -> {
                    deductionsRows.append(row);
                    customDeductTotal = customDeductTotal.add(
                            d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO);
                }
                default -> { /* skip unknowns */ }
            }
        }

        // Add statutory deductions
        if (run.getEmployeeEpf() != null && run.getEmployeeEpf().compareTo(BigDecimal.ZERO) > 0)
            deductionsRows.insert(0, tr("EPF — Employee", fmt(run.getEmployeeEpf())));
        if (run.getEtf() != null && run.getEtf().compareTo(BigDecimal.ZERO) > 0)
            deductionsRows.insert(0, tr("ETF", fmt(run.getEtf())));
        if (run.getPayeTax() != null && run.getPayeTax().compareTo(BigDecimal.ZERO) > 0)
            deductionsRows.insert(0, tr("PAYE Tax", fmt(run.getPayeTax())));

        String bodyStyle = landscape
                ? "display:grid;grid-template-columns:1fr 1fr;gap:24px;"
                : "display:flex;flex-direction:column;gap:16px;";

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8"/>
              <style>
                body { font-family: Arial, sans-serif; font-size: 13px; color: #1e293b; margin: 0; padding: 24px; }
                .card { max-width: 720px; margin: auto; border: 1px solid #e2e8f0; border-radius: 8px; padding: 24px; }
                .header { display: flex; justify-content: space-between; margin-bottom: 12px; }
                .company { font-size: 18px; font-weight: 700; }
                .period  { font-size: 13px; font-weight: 600; color: #475569; }
                hr { border: none; border-top: 1px solid #e2e8f0; margin: 12px 0; }
                .emp-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 4px 16px; margin-bottom: 8px; }
                .emp-row  { display: flex; gap: 8px; }
                .lbl { color: #64748b; min-width: 80px; }
                .val { font-weight: 500; }
                .section-title { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: .06em; color: #64748b; margin: 0 0 8px; }
                table { width: 100%; border-collapse: collapse; }
                td { padding: 3px 0; border-bottom: 1px solid #f1f5f9; }
                td:last-child { text-align: right; font-variant-numeric: tabular-nums; }
                .total-row td { font-weight: 600; border-top: 1px solid #cbd5e1; border-bottom: none; padding-top: 6px; }
                .net { display: flex; justify-content: space-between; align-items: center; margin-top: 16px; }
                .net-label { font-size: 15px; font-weight: 600; }
                .net-amount { font-size: 18px; font-weight: 700; color: #166534; }
              </style>
            </head>
            <body>
              <div class="card">
                <div class="header">
                  <div class="company">Ideal Pay<br><span style="font-size:11px;font-weight:400;color:#94a3b8;text-transform:uppercase;letter-spacing:.06em">Pay Slip</span></div>
                  <div class="period">%s</div>
                </div>
                <hr/>
                <div class="emp-grid">
                  <div class="emp-row"><span class="lbl">Employee</span><span class="val">%s</span></div>
                  <div class="emp-row"><span class="lbl">Emp No.</span><span class="val">%s</span></div>
                  %s
                  <div class="emp-row"><span class="lbl">Working Days</span><span class="val">%s</span></div>
                </div>
                <hr/>
                <div style="%s">
                  <div>
                    <p class="section-title">Earnings</p>
                    <table>
                      <tr><td>Basic Salary</td><td>%s</td></tr>
                      %s
                      <tr class="total-row"><td>Gross Pay</td><td>%s</td></tr>
                    </table>
                  </div>
                  <div>
                    <p class="section-title">Deductions</p>
                    <table>
                      %s
                      <tr class="total-row"><td>Total Deductions</td><td>%s</td></tr>
                    </table>
                  </div>
                </div>
                <hr/>
                <div class="net">
                  <span class="net-label">Net Pay</span>
                  <span class="net-amount">LKR %s</span>
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                period,
                empName,
                empCode,
                desgn.isBlank() ? "" : "<div class=\"emp-row\"><span class=\"lbl\">Designation</span><span class=\"val\">" + desgn + "</span></div>",
                run.getWorkingDays(),
                bodyStyle,
                fmt(run.getBasicSalary()),
                earningsRows,
                fmt(run.getGrossPay()),
                deductionsRows,
                fmt(run.getTotalDeductions()),
                fmt(run.getNetPay())
        );
    }

    private static String tr(String label, String amount) {
        return "<tr><td>" + label + "</td><td>" + amount + "</td></tr>";
    }

    private static String fmt(BigDecimal val) {
        if (val == null) return "0.00";
        return String.format("%,.2f", val);
    }

    private static String formatMonth(String payrollMonth) {
        if (payrollMonth == null || payrollMonth.length() < 7) return payrollMonth;
        try {
            String[] parts = payrollMonth.split("-");
            int year  = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            return Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year;
        } catch (Exception e) {
            return payrollMonth;
        }
    }
}
