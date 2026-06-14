-- ============================================================
-- V24 — Bonus Processing Module
--   1. Create bonus_processing_batch table (groups a bonus run)
--   2. Extend emp_bonus with status, adjusted amount, formula
--      result, approval, and batch FK columns
-- MySQL 8.0  |  ENGINE=InnoDB  |  utf8mb4_unicode_ci
-- ============================================================

-- ── 1. Bonus Processing Batch ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bonus_processing_batch (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    bonus_id        BIGINT        NOT NULL,
    payroll_month   VARCHAR(20)   NOT NULL  COMMENT 'YYYY-MM',
    status          VARCHAR(20)   NOT NULL  DEFAULT 'PENDING'
                                  COMMENT 'PENDING | APPROVED | PROCESSED | CANCELLED',
    employee_count  INT           NOT NULL  DEFAULT 0,
    total_amount    DECIMAL(15,2) NOT NULL  DEFAULT 0.00,
    notes           VARCHAR(500)  DEFAULT NULL,

    created_by      BIGINT        NOT NULL,
    created_date    TIMESTAMP     NOT NULL  DEFAULT CURRENT_TIMESTAMP,
    approved_by     BIGINT        DEFAULT NULL,
    approved_date   TIMESTAMP     DEFAULT NULL,
    processed_by    BIGINT        DEFAULT NULL,
    processed_date  TIMESTAMP     DEFAULT NULL,
    modified_by     BIGINT        NOT NULL,
    modified_date   TIMESTAMP     NOT NULL  DEFAULT CURRENT_TIMESTAMP
                                            ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY fk_bpb_bonus       (bonus_id),
    KEY fk_bpb_created_by  (created_by),
    KEY fk_bpb_approved_by (approved_by),
    KEY fk_bpb_modified_by (modified_by),
    CONSTRAINT fk_bpb_bonus        FOREIGN KEY (bonus_id)       REFERENCES bonus (id),
    CONSTRAINT fk_bpb_created_by   FOREIGN KEY (created_by)     REFERENCES usr   (id),
    CONSTRAINT fk_bpb_approved_by  FOREIGN KEY (approved_by)    REFERENCES usr   (id),
    CONSTRAINT fk_bpb_modified_by  FOREIGN KEY (modified_by)    REFERENCES usr   (id),
    CONSTRAINT chk_bpb_status      CHECK (status IN ('PENDING','APPROVED','PROCESSED','CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── 2. Extend emp_bonus ───────────────────────────────────────────────────────

-- Link each emp_bonus row to a processing batch
ALTER TABLE emp_bonus
    ADD COLUMN IF NOT EXISTS processing_batch_id BIGINT DEFAULT NULL
        COMMENT 'FK to bonus_processing_batch'
        AFTER bonus_id;

-- Workflow status (replaces the binary is_processed flag for managed bonuses)
ALTER TABLE emp_bonus
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        COMMENT 'PENDING | APPROVED | PROCESSED | CANCELLED'
        AFTER processing_batch_id;

-- Amount after optional manual adjustment
ALTER TABLE emp_bonus
    ADD COLUMN IF NOT EXISTS adjusted_amount DECIMAL(15,2) DEFAULT NULL
        COMMENT 'Override amount set by approver; NULL means use calculated amount'
        AFTER amount;

-- Formula expression that produced the calculated amount
ALTER TABLE emp_bonus
    ADD COLUMN IF NOT EXISTS formula_expression VARCHAR(500) DEFAULT NULL
        COMMENT 'MVEL expression used during calculation'
        AFTER adjusted_amount;

-- Raw formula output before rounding / adjustment
ALTER TABLE emp_bonus
    ADD COLUMN IF NOT EXISTS formula_result DECIMAL(15,2) DEFAULT NULL
        COMMENT 'Raw MVEL evaluation result'
        AFTER formula_expression;

-- Approval tracking
ALTER TABLE emp_bonus
    ADD COLUMN IF NOT EXISTS approved_by   BIGINT    DEFAULT NULL AFTER formula_result,
    ADD COLUMN IF NOT EXISTS approved_date TIMESTAMP DEFAULT NULL AFTER approved_by;

-- Audit note (for adjustments)
ALTER TABLE emp_bonus
    ADD COLUMN IF NOT EXISTS note VARCHAR(500) DEFAULT NULL
        COMMENT 'Reason for adjustment or other audit note'
        AFTER approved_date;

-- FK for the batch link
ALTER TABLE emp_bonus
    ADD CONSTRAINT fk_emp_bonus_batch
        FOREIGN KEY (processing_batch_id) REFERENCES bonus_processing_batch (id);

-- FK for approver
ALTER TABLE emp_bonus
    ADD CONSTRAINT fk_emp_bonus_approved_by
        FOREIGN KEY (approved_by) REFERENCES usr (id);

-- Index for batch lookups
ALTER TABLE emp_bonus
    ADD INDEX idx_emp_bonus_batch (processing_batch_id);

ALTER TABLE emp_bonus
    ADD CONSTRAINT chk_emp_bonus_status
        CHECK (status IN ('PENDING','APPROVED','PROCESSED','CANCELLED'));
