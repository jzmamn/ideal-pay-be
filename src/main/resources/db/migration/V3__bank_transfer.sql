-- ============================================================
-- V3 — Bank Transfer: template store + transfer audit log
-- MySQL 8.0  |  Flyway migration
-- ============================================================

-- ── 0. employee — add bank / account columns ─────────────────────────────────
-- IF NOT EXISTS is supported in MySQL 8.0+ and is safe to use directly via JDBC.
ALTER TABLE employee ADD COLUMN IF NOT EXISTS bank_id        BIGINT      NULL AFTER basic_salary;
ALTER TABLE employee ADD COLUMN IF NOT EXISTS bank_branch_id BIGINT      NULL AFTER bank_id;
ALTER TABLE employee ADD COLUMN IF NOT EXISTS account_no     VARCHAR(50) NULL AFTER bank_branch_id;

-- ── 1. bank_transfer_template ─────────────────────────────────────────────────
--   One row per bank.  Stores the free-form header/detail/footer templates
--   used to generate the bank's text file.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bank_transfer_template (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    bank_id          BIGINT         NOT NULL,
    bank_code        VARCHAR(20)    NOT NULL,
    bank_name        VARCHAR(100)   NOT NULL,
    header_template  TEXT           NULL     COMMENT 'Optional file header. Supports {{date}} {{bank_name}} {{bank_code}} {{record_count}} {{total_amount}}',
    detail_template  TEXT           NOT NULL COMMENT 'One output line per employee. Supports {{employee_no}} {{name}} {{account_no}} {{bank_code}} {{branch_code}} {{amount}} {{date}}',
    footer_template  TEXT           NULL     COMMENT 'Optional file footer. Supports {{record_count}} {{total_amount}} {{date}}',
    file_extension   VARCHAR(10)    NOT NULL DEFAULT 'txt',

    created_by       BIGINT         NOT NULL,
    created_date     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by      BIGINT         NOT NULL,
    modified_date    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_bank_transfer_template  PRIMARY KEY (id),
    CONSTRAINT uk_bank_transfer_template  UNIQUE      (bank_id),
    CONSTRAINT fk_btt_bank               FOREIGN KEY (bank_id) REFERENCES bank (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── 2. emp_transfer_log ───────────────────────────────────────────────────────
--   One row per transferred payroll run.
--   Prevents the same run from being transferred twice and provides an audit trail.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS emp_transfer_log (
    id                  BIGINT         NOT NULL AUTO_INCREMENT,
    payroll_run_id      BIGINT         NOT NULL,
    transfer_type       VARCHAR(20)    NOT NULL COMMENT 'SALARY | SALARY_ADVANCE | FIXED_ALLOWANCE',
    bank_id             BIGINT         NULL,
    bank_code           VARCHAR(20)    NULL,
    transferred_amount  DECIMAL(15,2)  NOT NULL DEFAULT 0.00,
    transferred_date    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    transferred_by      BIGINT         NOT NULL,

    CONSTRAINT pk_emp_transfer_log         PRIMARY KEY (id),
    CONSTRAINT uk_emp_transfer_log         UNIQUE      (payroll_run_id, transfer_type),
    CONSTRAINT fk_etl_payroll_run          FOREIGN KEY (payroll_run_id) REFERENCES emp_payroll_run (id),
    CONSTRAINT fk_etl_bank                 FOREIGN KEY (bank_id)        REFERENCES bank (id),
    CONSTRAINT fk_etl_transferred_by       FOREIGN KEY (transferred_by) REFERENCES usr (id),
    CONSTRAINT chk_etl_transfer_type       CHECK       (transfer_type IN ('SALARY','SALARY_ADVANCE','FIXED_ALLOWANCE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── 3. Seed: default templates for Sri Lankan banks ──────────────────────────
--   Pipe-delimited for local banks; CSV for SCB and HSBC.
--   Fine-tune per bank via the "Manage Templates" UI at any time.
--   bank_id values match the bank table above.
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO bank_transfer_template
    (bank_id, bank_code, bank_name, header_template, detail_template, footer_template, file_extension, created_by, modified_by)
VALUES
--  bank_id  code    name                                        header                                                          detail                                                                    footer                               ext   cr  mod
    (1,  '7010', 'Bank of Ceylon',                              'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1),
    (2,  '7038', 'Standard Chartered Bank',                    NULL,                                                          '{{account_no}},{{branch_code}},{{amount}},{{employee_no}},{{name}}',   NULL,                                 'csv', 1, 1),
    (4,  '7056', 'Commercial Bank Of Ceylon PLC',              'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1),
    (6,  '7083', 'Hatton National Bank PLC',                   'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1),
    (7,  '7092', 'Hongkong and Shanghai Banking Corporation',  NULL,                                                          '{{account_no}},{{branch_code}},{{amount}},{{employee_no}},{{name}}',   NULL,                                 'csv', 1, 1),
    (10, '7135', 'People''s Bank',                             'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1),
    (12, '7162', 'Nations Trust Bank PLC',                     'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1),
    (14, '7214', 'National Development Bank PLC',              'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1),
    (16, '7278', 'Sampath Bank PLC',                           'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1),
    (17, '7287', 'Seylan Bank PLC',                            'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1),
    (19, '7302', 'Union Bank Of Colombo PLC',                  'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1),
    (21, '7454', 'DFCC Bank PLC',                              'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1),
    (31, '7719', 'National Savings Bank',                      'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1),
    (33, '7737', 'HDFC Bank',                                  'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1);
