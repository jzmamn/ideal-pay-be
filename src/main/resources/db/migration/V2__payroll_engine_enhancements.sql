-- ============================================================
-- V2 — Payroll Calculation Engine Enhancements
-- MySQL 8.0  |  Safe to re-run (idempotent)
-- Uses information_schema checks — no MariaDB-only syntax
-- ============================================================

-- ============================================================
-- Helper procedure: adds a column only if it does not exist
-- ============================================================
DROP PROCEDURE IF EXISTS add_column_if_missing;

DELIMITER $$
CREATE PROCEDURE add_column_if_missing(
    IN p_table  VARCHAR(64),
    IN p_col    VARCHAR(64),
    IN p_def    TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = p_table
          AND COLUMN_NAME  = p_col
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN ', p_def);
        PREPARE s FROM @ddl;
        EXECUTE s;
        DEALLOCATE PREPARE s;
    END IF;
END$$
DELIMITER ;

-- ============================================================
-- 0a. emp_payroll_run_detail — make component_id nullable
-- ============================================================
ALTER TABLE emp_payroll_run_detail
    MODIFY COLUMN component_id BIGINT NULL;

-- ============================================================
-- 0b. emp_payroll_run_detail — drop uk_run_detail if exists
-- ============================================================
SET @drop_idx = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'emp_payroll_run_detail'
      AND INDEX_NAME   = 'uk_run_detail'
);
SET @sql_drop = IF(@drop_idx > 0,
    'ALTER TABLE emp_payroll_run_detail DROP INDEX uk_run_detail',
    'SELECT 1'
);
PREPARE stmt FROM @sql_drop;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 1. overtime
-- ============================================================
CALL add_column_if_missing('overtime', 'liable_for_epf',  "CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Y = OT included in EPF base'");
CALL add_column_if_missing('overtime', 'liable_for_etf',  "CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Y = OT included in ETF base'");
CALL add_column_if_missing('overtime', 'liable_for_paye', "CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Y = OT included in PAYE base'");

-- ============================================================
-- 2. nopay_days
-- ============================================================
CALL add_column_if_missing('nopay_days', 'liable_no_pay',   "CHAR(1)      NOT NULL DEFAULT 'Y' COMMENT 'Y = nopay deducted from EPF base'");
CALL add_column_if_missing('nopay_days', 'formula',         "VARCHAR(500) NULL COMMENT 'MVEL expression e.g. basicSalary / workingDays * NP_1'");
CALL add_column_if_missing('nopay_days', 'formula_enabled', "CHAR(1)      NOT NULL DEFAULT 'N' COMMENT 'Y = evaluate formula at run time'");

-- ============================================================
-- 3. payroll_period
-- ============================================================
CALL add_column_if_missing('payroll_period', 'working_days', "INT NOT NULL DEFAULT 26 COMMENT 'Payable days in this period'");

-- ============================================================
-- 4. emp_payroll_run
-- ============================================================
CALL add_column_if_missing('emp_payroll_run', 'epf_liable_base', "DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'Base for EPF/ETF computation'");
CALL add_column_if_missing('emp_payroll_run', 'employee_epf',    "DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'Employee EPF 8%'");
CALL add_column_if_missing('emp_payroll_run', 'employer_epf',    "DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'Employer EPF 12%'");
CALL add_column_if_missing('emp_payroll_run', 'etf',             "DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'ETF 3%'");
CALL add_column_if_missing('emp_payroll_run', 'paye_tax',        "DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'PAYE income tax'");
CALL add_column_if_missing('emp_payroll_run', 'working_days',    "INT           NOT NULL DEFAULT 26   COMMENT 'Working days used for this run'");

-- ============================================================
-- Cleanup
-- ============================================================
DROP PROCEDURE IF EXISTS add_column_if_missing;

-- ============================================================
-- 5. emp_payroll_run_detail — extend chk_detail_component_type
--    to include EPF_EE, EPF_ER, ETF, PAYE
-- ============================================================
SET @drop_chk1 = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME        = 'emp_payroll_run_detail'
      AND CONSTRAINT_NAME   = 'chk_detail_component_type'
      AND CONSTRAINT_TYPE   = 'CHECK'
);
SET @sql1 = IF(@drop_chk1 > 0,
    'ALTER TABLE emp_payroll_run_detail DROP CHECK chk_detail_component_type',
    'SELECT 1'
);
PREPARE s1 FROM @sql1; EXECUTE s1; DEALLOCATE PREPARE s1;

ALTER TABLE emp_payroll_run_detail
    ADD CONSTRAINT chk_detail_component_type
        CHECK (component_type IN ('FA','FD','VA','VD','OT','NOPAY','EPF_EE','EPF_ER','ETF','PAYE','SA'));

-- ============================================================
-- 6. emp_payroll_run — extend chk_run_status
--    to include CORRECTION_DRAFT, CORRECTION_LOCKED
-- ============================================================
SET @drop_chk2 = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME        = 'emp_payroll_run'
      AND CONSTRAINT_NAME   = 'chk_run_status'
      AND CONSTRAINT_TYPE   = 'CHECK'
);
SET @sql2 = IF(@drop_chk2 > 0,
    'ALTER TABLE emp_payroll_run DROP CHECK chk_run_status',
    'SELECT 1'
);
PREPARE s2 FROM @sql2; EXECUTE s2; DEALLOCATE PREPARE s2;

ALTER TABLE emp_payroll_run
    ADD CONSTRAINT chk_run_status
        CHECK (status IN ('DRAFT','PROCESSED','LOCKED','CORRECTION_DRAFT','CORRECTION_LOCKED'));

-- ============================================================
-- Verification — should return 13 rows
-- ============================================================
SELECT TABLE_NAME, COLUMN_NAME, IS_NULLABLE, COLUMN_DEFAULT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('overtime','nopay_days','payroll_period','emp_payroll_run','emp_payroll_run_detail')
  AND COLUMN_NAME IN (
      'liable_for_epf','liable_for_etf','liable_for_paye',
      'liable_no_pay','formula','formula_enabled',
      'working_days','epf_liable_base','employee_epf',
      'employer_epf','etf','paye_tax','component_id'
  )
ORDER BY TABLE_NAME, COLUMN_NAME;
