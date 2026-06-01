-- ============================================================
-- emp_bonus
-- ============================================================
DROP TABLE IF EXISTS `emp_bonus`;
CREATE TABLE `emp_bonus` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `emp_id` bigint NOT NULL,
  `payroll_month` varchar(20) NOT NULL,
  `amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `is_processed` char(1) NOT NULL DEFAULT 'N',
  `processed_date` datetime DEFAULT NULL,
  `created_by` bigint NOT NULL,
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_by` bigint NOT NULL,
  `modified_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emp_bonus` (`emp_id`,`payroll_month`),
  KEY `fk_emp_bonus_emp_id` (`emp_id`),
  KEY `fk_emp_bonus_created_by` (`created_by`),
  KEY `fk_emp_bonus_modified_by` (`modified_by`),
  KEY `idx_emp_bonus_month` (`emp_id`,`payroll_month`),
  CONSTRAINT `fk_emp_bonus_emp_id` FOREIGN KEY (`emp_id`) REFERENCES `employee` (`id`),
  CONSTRAINT `fk_emp_bonus_created_by` FOREIGN KEY (`created_by`) REFERENCES `usr` (`id`),
  CONSTRAINT `fk_emp_bonus_modified_by` FOREIGN KEY (`modified_by`) REFERENCES `usr` (`id`),
  CONSTRAINT `chk_emp_bonus_processed` CHECK (`is_processed` IN ('Y','N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `emp_bonus` (id, emp_id, payroll_month, amount, is_processed, processed_date, created_by, created_date, modified_by, modified_date) VALUES
(-1, -1, 'MMYYYY', 0.00, 'N', NULL, -1, '2026-05-17 12:34:46', -1, '2026-05-17 12:34:46'),
(1,   1, '2026-01', 50000.00, 'Y', '2026-05-25 08:23:02', 1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(2,   2, '2026-01', 30000.00, 'N', NULL,                  1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(3,   3, '2026-01', 20000.00, 'N', NULL,                  1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(4,   4, '2026-01', 45000.00, 'Y', '2026-05-25 08:23:02', 1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(5,   5, '2026-01', 15000.00, 'N', NULL,                  1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(6,   6, '2026-02', 60000.00, 'Y', '2026-05-25 08:23:02', 1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(7,   7, '2026-02', 25000.00, 'N', NULL,                  1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(8,   8, '2026-02', 35000.00, 'Y', '2026-05-25 08:23:02', 1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(9,   9, '2026-02', 18000.00, 'N', NULL,                  1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(10, 10, '2026-02', 40000.00, 'Y', '2026-05-25 08:23:02', 1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02');

-- ============================================================
-- emp_loan
-- ============================================================
DROP TABLE IF EXISTS `emp_loan`;
CREATE TABLE `emp_loan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `emp_id` bigint NOT NULL,
  `loan_id` bigint NOT NULL,
  `payroll_month` varchar(20) NOT NULL,
  `amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `is_processed` char(1) NOT NULL DEFAULT 'N',
  `processed_date` datetime DEFAULT NULL,
  `created_by` bigint NOT NULL,
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_by` bigint NOT NULL,
  `modified_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emp_loan` (`emp_id`,`loan_id`,`payroll_month`),
  KEY `fk_emp_loan_emp_id` (`emp_id`),
  KEY `fk_emp_loan_loan_id` (`loan_id`),
  KEY `fk_emp_loan_created_by` (`created_by`),
  KEY `fk_emp_loan_modified_by` (`modified_by`),
  KEY `idx_emp_loan_month` (`emp_id`,`payroll_month`),
  CONSTRAINT `fk_emp_loan_emp_id` FOREIGN KEY (`emp_id`) REFERENCES `employee` (`id`),
  CONSTRAINT `fk_emp_loan_loan_id` FOREIGN KEY (`loan_id`) REFERENCES `loan` (`id`),
  CONSTRAINT `fk_emp_loan_created_by` FOREIGN KEY (`created_by`) REFERENCES `usr` (`id`),
  CONSTRAINT `fk_emp_loan_modified_by` FOREIGN KEY (`modified_by`) REFERENCES `usr` (`id`),
  CONSTRAINT `chk_emp_loan_processed` CHECK (`is_processed` IN ('Y','N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `emp_loan` (id, emp_id, loan_id, payroll_month, amount, is_processed, processed_date, created_by, created_date, modified_by, modified_date) VALUES
(-1, -1, -1, 'MMYYYY', 0.00, 'N', NULL, -1, '2026-05-17 12:34:46', -1, '2026-05-17 12:34:46'),
(1,   1, -1, '2026-01', 5000.00, 'Y', '2026-05-25 08:23:02', 1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(2,   2, -1, '2026-01', 3000.00, 'N', NULL,                  1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(3,   3, -1, '2026-01', 4000.00, 'N', NULL,                  1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(4,   4, -1, '2026-01', 2500.00, 'Y', '2026-05-25 08:23:02', 1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(5,   5, -1, '2026-01', 6000.00, 'N', NULL,                  1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(6,   6, -1, '2026-02', 5000.00, 'Y', '2026-05-25 08:23:02', 1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(7,   7, -1, '2026-02', 3500.00, 'N', NULL,                  1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(8,   8, -1, '2026-02', 4500.00, 'Y', '2026-05-25 08:23:02', 1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(9,   9, -1, '2026-02', 2000.00, 'N', NULL,                  1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(10, 10, -1, '2026-02', 7000.00, 'Y', '2026-05-25 08:23:02', 1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02');

-- ============================================================
-- emp_sal_incr
-- ============================================================
DROP TABLE IF EXISTS `emp_sal_incr`;
CREATE TABLE `emp_sal_incr` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `emp_id` bigint NOT NULL,
  `payroll_month` varchar(20) NOT NULL,
  `amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `is_processed` char(1) NOT NULL DEFAULT 'N',
  `processed_date` datetime DEFAULT NULL,
  `created_by` bigint NOT NULL,
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_by` bigint NOT NULL,
  `modified_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emp_sal_incr` (`emp_id`,`payroll_month`),
  KEY `fk_emp_sal_incr_emp_id` (`emp_id`),
  KEY `fk_emp_sal_incr_created_by` (`created_by`),
  KEY `fk_emp_sal_incr_modified_by` (`modified_by`),
  KEY `idx_emp_sal_incr_month` (`emp_id`,`payroll_month`),
  CONSTRAINT `fk_emp_sal_incr_emp_id` FOREIGN KEY (`emp_id`) REFERENCES `employee` (`id`),
  CONSTRAINT `fk_emp_sal_incr_created_by` FOREIGN KEY (`created_by`) REFERENCES `usr` (`id`),
  CONSTRAINT `fk_emp_sal_incr_modified_by` FOREIGN KEY (`modified_by`) REFERENCES `usr` (`id`),
  CONSTRAINT `chk_emp_sal_incr_processed` CHECK (`is_processed` IN ('Y','N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `emp_sal_incr` (id, emp_id, payroll_month, amount, is_processed, processed_date, created_by, created_date, modified_by, modified_date) VALUES
(-1, -1, 'MMYYYY', 0.00, 'N', NULL, -1, '2026-05-17 12:34:46', -1, '2026-05-17 12:34:46'),
(1,   1, '2026-01', 10000.00, 'Y', '2026-05-25 08:23:02', 1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(2,   2, '2026-01',  8000.00, 'N', NULL,                  1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(3,   3, '2026-01',  5000.00, 'N', NULL,                  1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(4,   4, '2026-01', 12000.00, 'Y', '2026-05-25 08:23:02', 1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(5,   5, '2026-01',  6000.00, 'N', NULL,                  1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(6,   6, '2026-02', 15000.00, 'Y', '2026-05-25 08:23:02', 1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(7,   7, '2026-02',  7000.00, 'N', NULL,                  1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(8,   8, '2026-02',  9000.00, 'Y', '2026-05-25 08:23:02', 1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(9,   9, '2026-02',  4000.00, 'N', NULL,                  1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02'),
(10, 10, '2026-02', 11000.00, 'Y', '2026-05-25 08:23:02', 1, '2026-05-25 02:53:02', 1, '2026-05-25 02:53:02');

-- ============================================================
-- Stored Procedures
-- ============================================================

DROP PROCEDURE IF EXISTS `sp_emp_bonus_pivot`;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_emp_bonus_pivot`(IN p_payroll_month VARCHAR(20))
BEGIN
    DECLARE v_sql LONGTEXT DEFAULT '';
    SET v_sql = CONCAT(
        'SELECT
            e.id,
            e.employee_no,
            e.first_name,
            e.last_name,
            e.payroll_name,
            e.basic_salary,
            IFNULL(b.amount, 0)            AS bonus_amount,
            ''Bonus''                      AS bonus_label,
            IFNULL(b.is_processed, ''N'')  AS is_processed,
            b.processed_date
        FROM employee e
        LEFT JOIN emp_bonus b
            ON e.id = b.emp_id
            AND b.payroll_month = ''', p_payroll_month, '''
        WHERE e.is_active = ''Y'' AND e.id > 0
        ORDER BY e.id'
    );
    SET @stmt = v_sql; PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;
END ;;
DELIMITER ;

DROP PROCEDURE IF EXISTS `sp_emp_loan_pivot`;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_emp_loan_pivot`(IN p_payroll_month VARCHAR(20))
BEGIN
    DECLARE v_columns LONGTEXT DEFAULT '';
    DECLARE v_sql     LONGTEXT DEFAULT '';
    SET SESSION group_concat_max_len = 1000000;
    SELECT GROUP_CONCAT(
        DISTINCT CONCAT(
            'SUM(CASE WHEN l.id = ', l.id,
            ' THEN IFNULL(el.amount, 0) ELSE 0 END) AS `', l.code, '`, ',
            '''', REPLACE(REPLACE(l.name, '''', ''''''), '`', ''), ''' AS `', l.code, '_label`'
        ) ORDER BY l.id SEPARATOR ', '
    ) INTO v_columns FROM loan l WHERE l.is_active = 'Y' AND l.id > 0;
    IF v_columns IS NULL OR v_columns = '' THEN
        SET v_columns = '0 AS no_active_components, NULL AS no_active_components_label';
    END IF;
    SET v_sql = CONCAT(
        'SELECT e.id, e.employee_no, e.first_name, e.last_name, e.payroll_name, e.basic_salary, ',
        v_columns,
        ' FROM employee e
        LEFT JOIN emp_loan el ON e.id = el.emp_id AND el.payroll_month = ''', p_payroll_month, '''
        LEFT JOIN loan l ON el.loan_id = l.id
        WHERE e.is_active = ''Y'' AND e.id > 0
        GROUP BY e.id, e.employee_no, e.first_name, e.last_name, e.payroll_name, e.basic_salary
        ORDER BY e.id'
    );
    SET @stmt = v_sql; PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;
END ;;
DELIMITER ;

DROP PROCEDURE IF EXISTS `sp_emp_sal_incr_pivot`;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_emp_sal_incr_pivot`(IN p_payroll_month VARCHAR(20))
BEGIN
    DECLARE v_sql LONGTEXT DEFAULT '';
    SET v_sql = CONCAT(
        'SELECT
            e.id,
            e.employee_no,
            e.first_name,
            e.last_name,
            e.payroll_name,
            e.basic_salary,
            IFNULL(si.amount, 0)            AS sal_incr_amount,
            ''Salary Increment''            AS sal_incr_label,
            IFNULL(si.is_processed, ''N'')  AS is_processed,
            si.processed_date
        FROM employee e
        LEFT JOIN emp_sal_incr si
            ON e.id = si.emp_id
            AND si.payroll_month = ''', p_payroll_month, '''
        WHERE e.is_active = ''Y'' AND e.id > 0
        ORDER BY e.id'
    );
    SET @stmt = v_sql; PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;
END ;;
DELIMITER ;
