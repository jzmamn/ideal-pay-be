-- ─────────────────────────────────────────────────────────────────────────────
-- Salary Increment Management Tables
-- ─────────────────────────────────────────────────────────────────────────────

-- Increment header  (one record per batch or individual run)
CREATE TABLE `salary_increment` (
  `id`              bigint       NOT NULL AUTO_INCREMENT,
  `code`            varchar(20)  NOT NULL,
  `name`            varchar(150) NOT NULL,
  `type`            enum('BATCH','INDIVIDUAL') NOT NULL DEFAULT 'INDIVIDUAL',
  `effective_month` varchar(20)  NOT NULL COMMENT 'YYYY-MM',
  `status`          enum('DRAFT','APPROVED','EXPORTED','CANCELLED') NOT NULL DEFAULT 'DRAFT',
  `remarks`         varchar(500)  DEFAULT NULL,
  `created_by`      bigint       NOT NULL,
  `created_date`    timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_by`     bigint       NOT NULL,
  `modified_date`   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_salary_increment_code` (`code`),
  KEY `fk_si_created_by`  (`created_by`),
  KEY `fk_si_modified_by` (`modified_by`),
  KEY `idx_si_month`       (`effective_month`),
  KEY `idx_si_status`      (`status`),
  CONSTRAINT `fk_si_created_by`  FOREIGN KEY (`created_by`)  REFERENCES `usr` (`id`),
  CONSTRAINT `fk_si_modified_by` FOREIGN KEY (`modified_by`) REFERENCES `usr` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Salary increment batch/individual header';

-- Per-employee increment detail
CREATE TABLE `salary_increment_detail` (
  `id`              bigint        NOT NULL AUTO_INCREMENT,
  `increment_id`    bigint        NOT NULL,
  `emp_id`          bigint        NOT NULL,
  `current_basic`   decimal(15,2) NOT NULL DEFAULT '0.00',
  `increment_basic` decimal(15,2) NOT NULL DEFAULT '0.00',
  `new_basic`       decimal(15,2) NOT NULL DEFAULT '0.00',
  `is_exported`     char(1)       NOT NULL DEFAULT 'N',
  `exported_date`   datetime      DEFAULT NULL,
  `remarks`         varchar(500)  DEFAULT NULL,
  `created_by`      bigint        NOT NULL,
  `created_date`    timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_by`     bigint        NOT NULL,
  `modified_date`   timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_si_detail` (`increment_id`, `emp_id`),
  KEY `fk_sid_emp`         (`emp_id`),
  KEY `fk_sid_created_by`  (`created_by`),
  KEY `fk_sid_modified_by` (`modified_by`),
  CONSTRAINT `fk_sid_increment`   FOREIGN KEY (`increment_id`) REFERENCES `salary_increment` (`id`),
  CONSTRAINT `fk_sid_emp`         FOREIGN KEY (`emp_id`)       REFERENCES `employee` (`id`),
  CONSTRAINT `fk_sid_created_by`  FOREIGN KEY (`created_by`)   REFERENCES `usr` (`id`),
  CONSTRAINT `fk_sid_modified_by` FOREIGN KEY (`modified_by`)  REFERENCES `usr` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Per-employee salary increment detail';

-- Fixed allowance increments per employee detail
CREATE TABLE `salary_increment_fa` (
  `id`               bigint        NOT NULL AUTO_INCREMENT,
  `detail_id`        bigint        NOT NULL,
  `fa_id`            bigint        NOT NULL,
  `current_amount`   decimal(15,2) NOT NULL DEFAULT '0.00',
  `increment_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `new_amount`       decimal(15,2) NOT NULL DEFAULT '0.00',
  `created_by`       bigint        NOT NULL,
  `created_date`     timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_by`      bigint        NOT NULL,
  `modified_date`    timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_si_fa` (`detail_id`, `fa_id`),
  KEY `fk_sifa_fa`         (`fa_id`),
  KEY `fk_sifa_created_by`  (`created_by`),
  KEY `fk_sifa_modified_by` (`modified_by`),
  CONSTRAINT `fk_sifa_detail`      FOREIGN KEY (`detail_id`)  REFERENCES `salary_increment_detail` (`id`),
  CONSTRAINT `fk_sifa_fa`          FOREIGN KEY (`fa_id`)      REFERENCES `fixed_allowance` (`id`),
  CONSTRAINT `fk_sifa_created_by`  FOREIGN KEY (`created_by`) REFERENCES `usr` (`id`),
  CONSTRAINT `fk_sifa_modified_by` FOREIGN KEY (`modified_by`) REFERENCES `usr` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Fixed-allowance increment per employee detail';

-- ─────────────────────────────────────────────────────────────────────────────
-- Sample Data
-- ─────────────────────────────────────────────────────────────────────────────

-- Disable FK checks so we can insert id = -1 default rows freely
SET FOREIGN_KEY_CHECKS = 0;

-- ── salary_increment ─────────────────────────────────────────────────────────
-- Columns: id, code, name, type, effective_month, status, remarks, created_by, created_date, modified_by, modified_date
INSERT INTO `salary_increment` VALUES
-- Default / system placeholder
(-1, 'DEFAULT', 'System Default Increment', 'INDIVIDUAL', 'MMYYYY', 'DRAFT', 'System default', -1, '2026-01-01 00:00:00', -1, '2026-01-01 00:00:00'),

-- SI001: Batch annual increment for 2026-01 — already EXPORTED
(1, 'SI001', 'Annual Increment 2026 - Batch', 'BATCH', '2026-01', 'EXPORTED', 'Annual salary revision for all permanent staff effective January 2026', 1, '2026-01-10 09:00:00', 1, '2026-01-20 14:30:00'),

-- SI002: Individual increment for emp 1 — APPROVED, pending export
(2, 'SI002', 'Mid-Year Increment - John Fernando', 'INDIVIDUAL', '2026-06', 'APPROVED', 'Performance-based increment for Q1 2026', 1, '2026-05-15 10:00:00', 1, '2026-05-20 11:00:00'),

-- SI003: Batch increment for 2026-07 — still DRAFT
(3, 'SI003', 'Mid-Year Increment 2026 - Batch', 'BATCH', '2026-07', 'DRAFT', 'Proposed mid-year increment for all branches', 1, '2026-05-28 08:30:00', 1, '2026-05-28 08:30:00'),

-- SI004: Individual increment — CANCELLED
(4, 'SI004', 'Probation Completion - Emp 3', 'INDIVIDUAL', '2026-03', 'CANCELLED', 'Cancelled — employee resigned before effective date', 1, '2026-02-10 09:00:00', 1, '2026-02-28 16:00:00');


-- ── salary_increment_detail ──────────────────────────────────────────────────
-- Columns: id, increment_id, emp_id, current_basic, increment_basic, new_basic,
--          is_exported, exported_date, remarks, created_by, created_date, modified_by, modified_date
INSERT INTO `salary_increment_detail` VALUES
-- Default row
(-1, -1, -1, 0.00, 0.00, 0.00, 'N', NULL, 'System default', -1, '2026-01-01 00:00:00', -1, '2026-01-01 00:00:00'),

-- SI001 EXPORTED batch — 5 employees
(1,  1, 1, 85000.00,  5000.00,  90000.00, 'Y', '2026-01-20 14:30:00', NULL, 1, '2026-01-10 09:00:00', 1, '2026-01-20 14:30:00'),
(2,  1, 2, 72000.00,  4000.00,  76000.00, 'Y', '2026-01-20 14:30:00', NULL, 1, '2026-01-10 09:00:00', 1, '2026-01-20 14:30:00'),
(3,  1, 3, 68000.00,  3500.00,  71500.00, 'Y', '2026-01-20 14:30:00', NULL, 1, '2026-01-10 09:00:00', 1, '2026-01-20 14:30:00'),
(4,  1, 4, 95000.00,  7500.00, 102500.00, 'Y', '2026-01-20 14:30:00', NULL, 1, '2026-01-10 09:00:00', 1, '2026-01-20 14:30:00'),
(5,  1, 5, 58000.00,  3000.00,  61000.00, 'Y', '2026-01-20 14:30:00', NULL, 1, '2026-01-10 09:00:00', 1, '2026-01-20 14:30:00'),

-- SI002 APPROVED individual — emp 1
(6,  2, 1, 90000.00, 10000.00, 100000.00, 'N', NULL, 'Outstanding Q1 performance rating', 1, '2026-05-15 10:00:00', 1, '2026-05-15 10:00:00'),

-- SI003 DRAFT batch — 3 employees
(7,  3, 1, 90000.00,  6000.00,  96000.00, 'N', NULL, NULL, 1, '2026-05-28 08:30:00', 1, '2026-05-28 08:30:00'),
(8,  3, 2, 76000.00,  4500.00,  80500.00, 'N', NULL, NULL, 1, '2026-05-28 08:30:00', 1, '2026-05-28 08:30:00'),
(9,  3, 3, 71500.00,  3500.00,  75000.00, 'N', NULL, NULL, 1, '2026-05-28 08:30:00', 1, '2026-05-28 08:30:00'),

-- SI004 CANCELLED individual — emp 3
(10, 4, 3, 68000.00,  5000.00,  73000.00, 'N', NULL, NULL, 1, '2026-02-10 09:00:00', 1, '2026-02-28 16:00:00');


-- ── salary_increment_fa ──────────────────────────────────────────────────────
-- Columns: id, detail_id, fa_id, current_amount, increment_amount, new_amount,
--          created_by, created_date, modified_by, modified_date
-- fa_id reference: 1=Transport Allowance, 2=Meal Allowance, 3=Housing Allowance, 4=Mobile Allowance
INSERT INTO `salary_increment_fa` VALUES
-- Default row
(-1, -1, -1, 0.00, 0.00, 0.00, -1, '2026-01-01 00:00:00', -1, '2026-01-01 00:00:00'),

-- SI001 detail 1 (emp 1) — Transport + Meal allowance increments
(1, 1, 1, 15000.00, 2000.00, 17000.00, 1, '2026-01-10 09:00:00', 1, '2026-01-20 14:30:00'),
(2, 1, 2, 12000.00, 1000.00, 13000.00, 1, '2026-01-10 09:00:00', 1, '2026-01-20 14:30:00'),

-- SI001 detail 2 (emp 2) — Transport allowance increment
(3, 2, 1, 12000.00, 1500.00, 13500.00, 1, '2026-01-10 09:00:00', 1, '2026-01-20 14:30:00'),

-- SI001 detail 3 (emp 3) — Meal allowance increment
(4, 3, 2,  8000.00, 1000.00,  9000.00, 1, '2026-01-10 09:00:00', 1, '2026-01-20 14:30:00'),

-- SI001 detail 4 (emp 4) — Transport + Housing increment
(5, 4, 1, 10000.00, 2000.00, 12000.00, 1, '2026-01-10 09:00:00', 1, '2026-01-20 14:30:00'),
(6, 4, 3,  5000.00, 2000.00,  7000.00, 1, '2026-01-10 09:00:00', 1, '2026-01-20 14:30:00'),

-- SI001 detail 5 (emp 5) — Mobile allowance increment
(7, 5, 4,  5000.00,  500.00,  5500.00, 1, '2026-01-10 09:00:00', 1, '2026-01-20 14:30:00'),

-- SI002 detail 6 (emp 1 APPROVED) — Transport + Meal + Mobile
(8,  6, 1, 17000.00, 3000.00, 20000.00, 1, '2026-05-15 10:00:00', 1, '2026-05-15 10:00:00'),
(9,  6, 2, 13000.00, 2000.00, 15000.00, 1, '2026-05-15 10:00:00', 1, '2026-05-15 10:00:00'),
(10, 6, 4,  2000.00,  500.00,  2500.00, 1, '2026-05-15 10:00:00', 1, '2026-05-15 10:00:00'),

-- SI003 detail 7 (emp 1 DRAFT) — Transport allowance
(11, 7, 1, 17000.00, 2000.00, 19000.00, 1, '2026-05-28 08:30:00', 1, '2026-05-28 08:30:00'),

-- SI003 detail 8 (emp 2 DRAFT) — Transport allowance
(12, 8, 1, 13500.00, 1500.00, 15000.00, 1, '2026-05-28 08:30:00', 1, '2026-05-28 08:30:00');

SET FOREIGN_KEY_CHECKS = 1;
