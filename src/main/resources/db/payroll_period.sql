-- ─────────────────────────────────────────────────────────────────────────────
-- Payroll Period Management
-- Run once against ideal_pay schema
-- ─────────────────────────────────────────────────────────────────────────────

-- 1. Extend emp_payroll_run with run_type and parent_run_id
ALTER TABLE emp_payroll_run
    ADD COLUMN run_type      VARCHAR(20) NOT NULL DEFAULT 'NORMAL'
        COMMENT 'NORMAL | CORRECTION',
    ADD COLUMN parent_run_id BIGINT      NULL
        COMMENT 'For CORRECTION runs: FK to the original LOCKED run';

ALTER TABLE emp_payroll_run
    ADD CONSTRAINT fk_epr_parent_run
        FOREIGN KEY (parent_run_id) REFERENCES emp_payroll_run(id)
        ON DELETE SET NULL;

-- Widen status to accommodate new values (VARCHAR already — just update the comment)
ALTER TABLE emp_payroll_run
    MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
    COMMENT 'DRAFT | PROCESSED | LOCKED | CORRECTION_DRAFT | CORRECTION_LOCKED';

-- 2. Create payroll_period table
CREATE TABLE IF NOT EXISTS payroll_period (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    period_month  VARCHAR(7)   NOT NULL COMMENT 'YYYY-MM',
    status        VARCHAR(10)  NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN | CLOSED',
    closed_date   TIMESTAMP    NULL,
    closed_by     BIGINT       NULL,
    created_by    BIGINT       NOT NULL,
    created_date  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT       NOT NULL,
    modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_payroll_period_month (period_month),
    CONSTRAINT fk_pp_closed_by  FOREIGN KEY (closed_by)  REFERENCES usr(id),
    CONSTRAINT fk_pp_created_by FOREIGN KEY (created_by) REFERENCES usr(id),
    CONSTRAINT fk_pp_modified_by FOREIGN KEY (modified_by) REFERENCES usr(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
