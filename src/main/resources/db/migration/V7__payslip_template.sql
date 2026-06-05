-- ─────────────────────────────────────────────────────────────────────────────
-- V7: Payslip HTML template support
--
-- 1. Add EPF/ETF registration numbers to the company table (shown on payslip).
-- 2. Create payslip_template table — stores one editable HTML template at a time.
-- ─────────────────────────────────────────────────────────────────────────────

-- ── 1. ALTER company ─────────────────────────────────────────────────────────

ALTER TABLE company
    ADD COLUMN epf_no VARCHAR(50) NULL COMMENT 'Company EPF registration number' AFTER logo,
    ADD COLUMN etf_no VARCHAR(50) NULL COMMENT 'Company ETF registration number' AFTER epf_no;


-- ── 2. CREATE payslip_template ────────────────────────────────────────────────
--
-- Single-row design: only one template is active at a time.
-- To update the template, either:
--   (a) UPDATE the active row's html_content, or
--   (b) INSERT a new row and set is_active = 'Y' on the new one (deactivate old).
--
-- {{TOKEN}} placeholders in html_content are replaced at PDF generation time.
-- See PayslipTokenMapper for the full token reference.

CREATE TABLE IF NOT EXISTS payslip_template (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    name          VARCHAR(150)  NOT NULL                   COMMENT 'Friendly label, e.g. "Standard Payslip v1"',
    html_content  LONGTEXT      NOT NULL                   COMMENT 'Plain HTML with {{TOKEN}} placeholders',
    is_active     CHAR(1)       NOT NULL DEFAULT 'Y'       COMMENT 'Y = used for PDF generation',
    created_by    BIGINT        NOT NULL,
    modified_by   BIGINT        NOT NULL,
    created_date  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_date TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_pt_created_by  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_pt_modified_by FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
