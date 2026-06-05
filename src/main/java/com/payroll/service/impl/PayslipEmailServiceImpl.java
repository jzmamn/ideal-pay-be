package com.payroll.service.impl;

import com.payroll.dto.request.PayslipEmailRequestDTO;
import com.payroll.dto.response.PayslipEmailResultDTO;
import com.payroll.entity.EmailConfig;
import com.payroll.entity.EmailTemplate;
import com.payroll.entity.EmpPayrollRun;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.repository.EmailConfigRepository;
import com.payroll.repository.EmailTemplateRepository;
import com.payroll.repository.EmpPayrollRunRepository;
import com.payroll.service.PayslipEmailService;
import com.payroll.service.PayslipPdfService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final EmailTemplateRepository templateRepo;
    private final PayslipPdfService       pdfService;

    @Override
    public PayslipEmailResultDTO sendPayslips(PayslipEmailRequestDTO request) {

        // Resolve email template (optional)
        EmailTemplate template = null;
        if (request.getTemplateId() != null) {
            template = templateRepo.findById(request.getTemplateId()).orElse(null);
        }

        // Resolve SMTP config: template's linked config → active config
        EmailConfig cfg;
        if (template != null && template.getEmailConfig() != null) {
            cfg = template.getEmailConfig();
        } else {
            cfg = emailConfigRepo.findTopByIsActiveTrueOrderByIdDesc()
                    .orElseThrow(() -> new ResourceNotFoundException("No email configuration found"));
        }

        JavaMailSenderImpl sender = EmailConfigServiceImpl.buildSender(cfg);

        int sent = 0, failed = 0;
        List<String> errors = new ArrayList<>();

        for (Long runId : request.getRunIds()) {
            EmpPayrollRun run = runRepository.findById(runId).orElse(null);
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
                String period  = formatMonth(run.getPayrollMonth());
                String empName = run.getEmployee().getFirstName() + " " + run.getEmployee().getLastName();

                byte[] pdfBytes = pdfService.renderSinglePdfBytes(runId, request.getPdfTemplateId());

                String subject;
                String bodyHtml;

                if (template != null) {
                    subject  = substituteVars(template.getSubject(), run, empName, period);
                    bodyHtml = substituteVars(template.getBody(),    run, empName, period);
                } else {
                    subject  = "Pay Slip — " + period;
                    bodyHtml = "<p>Dear " + empName + ",</p>"
                             + "<p>Please find your payslip for <strong>" + period + "</strong> attached.</p>";
                }

                MimeMessage msg = sender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
                helper.setFrom(cfg.getFromAddress(), cfg.getFromName());
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(bodyHtml, true);

                String filename = "Payslip_" + run.getEmployee().getEmployeeNo() + "_" + run.getPayrollMonth() + ".pdf";
                helper.addAttachment(filename,
                        () -> new java.io.ByteArrayInputStream(pdfBytes),
                        "application/pdf");

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

    private String substituteVars(String text, EmpPayrollRun run, String empName, String period) {
        String empNo   = run.getEmployee().getEmployeeNo();
        String[] parts = run.getPayrollMonth() != null ? run.getPayrollMonth().split("-") : new String[]{"", ""};
        return text
                .replace("{{employee_name}}",   empName)
                .replace("{{employee_no}}",      empNo)
                .replace("{{department}}",       "")
                .replace("{{company_name}}",     "Ideal Pay")
                .replace("{{month}}",            parts.length > 1 ? parts[1] : "")
                .replace("{{year}}",             parts.length > 0 ? parts[0] : "")
                .replace("{{net_pay}}",          fmt(run.getNetPay()))
                .replace("{{gross_pay}}",        fmt(run.getGrossPay()))
                .replace("{{total_deductions}}", fmt(run.getTotalDeductions()));
    }

    private static String fmt(java.math.BigDecimal val) {
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
