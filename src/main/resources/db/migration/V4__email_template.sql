-- ============================================================
-- V4 — Email Template store
-- MySQL 8.0  |  Flyway migration
-- ============================================================

CREATE TABLE IF NOT EXISTS email_template (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    name          VARCHAR(150)  NOT NULL                    COMMENT 'Human-readable template name',
    template_type VARCHAR(30)   NOT NULL                    COMMENT 'PAYSLIP | SALARY_ADVANCE | SALARY_INCREMENT | GENERAL',
    subject       VARCHAR(500)  NOT NULL                    COMMENT 'Email subject — supports {{variables}}',
    body          LONGTEXT      NOT NULL                    COMMENT 'HTML or plain-text body — supports {{variables}}',
    is_active     CHAR(1)       NOT NULL DEFAULT 'Y'        COMMENT 'Y/N',

    created_by    BIGINT        NOT NULL,
    created_date  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT        NOT NULL,
    modified_date TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_email_template   PRIMARY KEY (id),
    CONSTRAINT uk_email_template   UNIQUE      (name),
    CONSTRAINT chk_et_type         CHECK       (template_type IN ('PAYSLIP','SALARY_ADVANCE','SALARY_INCREMENT','GENERAL')),
    CONSTRAINT chk_et_active       CHECK       (is_active IN ('Y','N')),
    CONSTRAINT fk_et_created_by    FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_et_modified_by   FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Seed: starter templates ───────────────────────────────────────────────────
INSERT INTO email_template (name, template_type, subject, body, is_active, created_by, modified_by) VALUES
(
  'Payslip Notification',
  'PAYSLIP',
  'Your Payslip for {{month}} {{year}} – {{company_name}}',
  '<p>Dear {{employee_name}},</p>
<p>Please find attached your payslip for <strong>{{month}} {{year}}</strong>.</p>
<table style="border-collapse:collapse;width:360px">
  <tr><td style="padding:6px 12px;background:#f8fafc;font-weight:600">Employee No</td><td style="padding:6px 12px">{{employee_no}}</td></tr>
  <tr><td style="padding:6px 12px;background:#f8fafc;font-weight:600">Department</td><td style="padding:6px 12px">{{department}}</td></tr>
  <tr><td style="padding:6px 12px;background:#f8fafc;font-weight:600">Net Pay</td><td style="padding:6px 12px"><strong>{{net_pay}}</strong></td></tr>
</table>
<p style="margin-top:20px">If you have any questions, please contact HR.</p>
<p>Regards,<br>{{company_name}} – Payroll Team</p>',
  'Y', 1, 1
),
(
  'Salary Advance Approval',
  'SALARY_ADVANCE',
  'Salary Advance Approved – {{month}} {{year}}',
  '<p>Dear {{employee_name}},</p>
<p>Your salary advance request of <strong>{{advance_amount}}</strong> for {{month}} {{year}} has been approved.</p>
<p>The amount will be reflected in your next payslip.</p>
<p>Regards,<br>{{company_name}} – Payroll Team</p>',
  'Y', 1, 1
),
(
  'Salary Increment Notice',
  'SALARY_INCREMENT',
  'Salary Increment Effective {{effective_date}} – {{company_name}}',
  '<p>Dear {{employee_name}},</p>
<p>We are pleased to inform you that your salary has been revised effective <strong>{{effective_date}}</strong>.</p>
<table style="border-collapse:collapse;width:360px">
  <tr><td style="padding:6px 12px;background:#f8fafc;font-weight:600">Previous Salary</td><td style="padding:6px 12px">{{previous_salary}}</td></tr>
  <tr><td style="padding:6px 12px;background:#f8fafc;font-weight:600">New Salary</td><td style="padding:6px 12px"><strong>{{new_salary}}</strong></td></tr>
</table>
<p>Congratulations on this achievement.</p>
<p>Regards,<br>{{company_name}} – HR Team</p>',
  'Y', 1, 1
);
