-- =============================================================================
-- Corrected Pivot Stored Procedures — with display name label columns
-- Run this entire script in a single execution.
--
-- Each SP returns:
--   Fixed fields : id, employee_no, first_name, last_name, payroll_name, basic_salary
--   Per component: {code}         -> amount   (matrix key / save identifier)
--                  {code}_label   -> name     (display label in the UI)
-- OT additionally : {code}_hours
-- NoPay additionally: {code}_days
-- =============================================================================

DROP PROCEDURE IF EXISTS `sp_employee_fixed_allowance_pivot`;
DROP PROCEDURE IF EXISTS `sp_emp_fa_pivot`;
DROP PROCEDURE IF EXISTS `sp_emp_fd_pivot`;
DROP PROCEDURE IF EXISTS `sp_emp_va_pivot`;
DROP PROCEDURE IF EXISTS `sp_emp_vd_pivot`;
DROP PROCEDURE IF EXISTS `sp_emp_ot_pivot`;
DROP PROCEDURE IF EXISTS `sp_emp_np_pivot`;

DELIMITER $$

-- =============================================================================
-- Fixed Allowance Pivot
-- =============================================================================
CREATE PROCEDURE `sp_emp_fa_pivot`(IN p_payroll_month VARCHAR(20))
BEGIN
    DECLARE v_columns LONGTEXT DEFAULT '';
    DECLARE v_sql     LONGTEXT DEFAULT '';
    SET SESSION group_concat_max_len = 1000000;
    SELECT GROUP_CONCAT(
        DISTINCT CONCAT(
            'SUM(CASE WHEN fa.id = ', fa.id,
            ' THEN IFNULL(ef.amount, 0) ELSE 0 END) AS `', fa.code, '`, ',
            '''', REPLACE(REPLACE(fa.name, '''', ''''''), '`', ''), ''' AS `', fa.code, '_label`'
        ) ORDER BY fa.id SEPARATOR ', '
    ) INTO v_columns FROM fixed_allowance fa WHERE fa.is_active = 'Y' AND fa.id > 0;
    IF v_columns IS NULL OR v_columns = '' THEN
        SET v_columns = '0 AS no_active_components, NULL AS no_active_components_label';
    END IF;
    SET v_sql = CONCAT(
        'SELECT e.id, e.employee_no, e.first_name, e.last_name, e.payroll_name, e.basic_salary, ',
        v_columns,
        ' FROM employee e
        LEFT JOIN emp_fa ef ON e.id = ef.emp_id AND ef.payroll_month = ''', p_payroll_month, '''
        LEFT JOIN fixed_allowance fa ON ef.fa_id = fa.id
        WHERE e.is_active = ''Y'' AND e.id > 0
        GROUP BY e.id, e.employee_no, e.first_name, e.last_name, e.payroll_name, e.basic_salary
        ORDER BY e.id'
    );
    SET @stmt = v_sql; PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;
END$$

-- =============================================================================
-- Fixed Deduction Pivot
-- =============================================================================
CREATE PROCEDURE `sp_emp_fd_pivot`(IN p_payroll_month VARCHAR(20))
BEGIN
    DECLARE v_columns LONGTEXT DEFAULT '';
    DECLARE v_sql     LONGTEXT DEFAULT '';
    SET SESSION group_concat_max_len = 1000000;
    SELECT GROUP_CONCAT(
        DISTINCT CONCAT(
            'SUM(CASE WHEN fd.id = ', fd.id,
            ' THEN IFNULL(efd.amount, 0) ELSE 0 END) AS `', fd.code, '`, ',
            '''', REPLACE(REPLACE(fd.name, '''', ''''''), '`', ''), ''' AS `', fd.code, '_label`'
        ) ORDER BY fd.id SEPARATOR ', '
    ) INTO v_columns FROM fixed_deduction fd WHERE fd.is_active = 'Y' AND fd.id > 0;
    IF v_columns IS NULL OR v_columns = '' THEN
        SET v_columns = '0 AS no_active_components, NULL AS no_active_components_label';
    END IF;
    SET v_sql = CONCAT(
        'SELECT e.id, e.employee_no, e.first_name, e.last_name, e.payroll_name, e.basic_salary, ',
        v_columns,
        ' FROM employee e
        LEFT JOIN emp_fd efd ON e.id = efd.emp_id AND efd.payroll_month = ''', p_payroll_month, '''
        LEFT JOIN fixed_deduction fd ON efd.fd_id = fd.id
        WHERE e.is_active = ''Y'' AND e.id > 0
        GROUP BY e.id, e.employee_no, e.first_name, e.last_name, e.payroll_name, e.basic_salary
        ORDER BY e.id'
    );
    SET @stmt = v_sql; PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;
END$$

-- =============================================================================
-- Variable Allowance Pivot
-- =============================================================================
CREATE PROCEDURE `sp_emp_va_pivot`(IN p_payroll_month VARCHAR(20))
BEGIN
    DECLARE v_columns LONGTEXT DEFAULT '';
    DECLARE v_sql     LONGTEXT DEFAULT '';
    SET SESSION group_concat_max_len = 1000000;
    SELECT GROUP_CONCAT(
        DISTINCT CONCAT(
            'SUM(CASE WHEN va.id = ', va.id,
            ' THEN IFNULL(eva.amount, 0) ELSE 0 END) AS `', va.code, '`, ',
            '''', REPLACE(REPLACE(va.name, '''', ''''''), '`', ''), ''' AS `', va.code, '_label`'
        ) ORDER BY va.id SEPARATOR ', '
    ) INTO v_columns FROM variable_allowance va WHERE va.is_active = 'Y' AND va.id > 0;
    IF v_columns IS NULL OR v_columns = '' THEN
        SET v_columns = '0 AS no_active_components, NULL AS no_active_components_label';
    END IF;
    SET v_sql = CONCAT(
        'SELECT e.id, e.employee_no, e.first_name, e.last_name, e.payroll_name, e.basic_salary, ',
        v_columns,
        ' FROM employee e
        LEFT JOIN emp_va eva ON e.id = eva.emp_id AND eva.payroll_month = ''', p_payroll_month, '''
        LEFT JOIN variable_allowance va ON eva.va_id = va.id
        WHERE e.is_active = ''Y'' AND e.id > 0
        GROUP BY e.id, e.employee_no, e.first_name, e.last_name, e.payroll_name, e.basic_salary
        ORDER BY e.id'
    );
    SET @stmt = v_sql; PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;
END$$

-- =============================================================================
-- Variable Deduction Pivot
-- =============================================================================
CREATE PROCEDURE `sp_emp_vd_pivot`(IN p_payroll_month VARCHAR(20))
BEGIN
    DECLARE v_columns LONGTEXT DEFAULT '';
    DECLARE v_sql     LONGTEXT DEFAULT '';
    SET SESSION group_concat_max_len = 1000000;
    SELECT GROUP_CONCAT(
        DISTINCT CONCAT(
            'SUM(CASE WHEN vd.id = ', vd.id,
            ' THEN IFNULL(evd.amount, 0) ELSE 0 END) AS `', vd.code, '`, ',
            '''', REPLACE(REPLACE(vd.name, '''', ''''''), '`', ''), ''' AS `', vd.code, '_label`'
        ) ORDER BY vd.id SEPARATOR ', '
    ) INTO v_columns FROM variable_deduction vd WHERE vd.is_active = 'Y' AND vd.id > 0;
    IF v_columns IS NULL OR v_columns = '' THEN
        SET v_columns = '0 AS no_active_components, NULL AS no_active_components_label';
    END IF;
    SET v_sql = CONCAT(
        'SELECT e.id, e.employee_no, e.first_name, e.last_name, e.payroll_name, e.basic_salary, ',
        v_columns,
        ' FROM employee e
        LEFT JOIN emp_vd evd ON e.id = evd.emp_id AND evd.payroll_month = ''', p_payroll_month, '''
        LEFT JOIN variable_deduction vd ON evd.vd_id = vd.id
        WHERE e.is_active = ''Y'' AND e.id > 0
        GROUP BY e.id, e.employee_no, e.first_name, e.last_name, e.payroll_name, e.basic_salary
        ORDER BY e.id'
    );
    SET @stmt = v_sql; PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;
END$$

-- =============================================================================
-- Overtime Pivot — {code}_rate, {code}_hours, {code}_amount, {code}_label per OT type
-- =============================================================================
CREATE PROCEDURE `sp_emp_ot_pivot`(IN p_payroll_month VARCHAR(20))
BEGIN
    DECLARE v_columns LONGTEXT DEFAULT '';
    DECLARE v_sql     LONGTEXT DEFAULT '';
    SET SESSION group_concat_max_len = 1000000;
    SELECT GROUP_CONCAT(
        DISTINCT CONCAT(
            'SUM(CASE WHEN ot.id = ', ot.id, ' THEN IFNULL(eot.rate,   0) ELSE 0 END) AS `', ot.code, '_rate`,  ',
            'SUM(CASE WHEN ot.id = ', ot.id, ' THEN IFNULL(eot.hours,  0) ELSE 0 END) AS `', ot.code, '_hours`, ',
            'SUM(CASE WHEN ot.id = ', ot.id, ' THEN IFNULL(eot.amount, 0) ELSE 0 END) AS `', ot.code, '_amount`,',
            ' ''', REPLACE(REPLACE(ot.name, '''', ''''''), '`', ''), ''' AS `', ot.code, '_label`'
        ) ORDER BY ot.id SEPARATOR ', '
    ) INTO v_columns FROM overtime ot WHERE ot.is_active = 'Y' AND ot.id > 0;
    IF v_columns IS NULL OR v_columns = '' THEN
        SET v_columns = '0 AS no_active_ot, 0 AS no_active_ot_hours, 0 AS no_active_ot_amount, NULL AS no_active_ot_label';
    END IF;
    SET v_sql = CONCAT(
        'SELECT e.id, e.employee_no, e.first_name, e.last_name, e.payroll_name, e.basic_salary, ',
        v_columns,
        ' FROM employee e
        LEFT JOIN emp_ot eot ON e.id = eot.emp_id AND eot.payroll_month = ''', p_payroll_month, '''
        LEFT JOIN overtime ot ON eot.overtime_id = ot.id
        WHERE e.is_active = ''Y'' AND e.id > 0
        GROUP BY e.id, e.employee_no, e.first_name, e.last_name, e.payroll_name, e.basic_salary
        ORDER BY e.id'
    );
    SET @stmt = v_sql; PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;
END$$

-- =============================================================================
-- NoPay Pivot — {code}_rate, {code}_days, {code}_amount, {code}_label per NoPay type
-- =============================================================================
CREATE PROCEDURE `sp_emp_np_pivot`(IN p_payroll_month VARCHAR(20))
BEGIN
    DECLARE v_columns LONGTEXT DEFAULT '';
    DECLARE v_sql     LONGTEXT DEFAULT '';
    SET SESSION group_concat_max_len = 1000000;
    SELECT GROUP_CONCAT(
        DISTINCT CONCAT(
            'SUM(CASE WHEN nd.id = ', nd.id, ' THEN IFNULL(enp.rate,   0) ELSE 0 END) AS `', nd.code, '_rate`,  ',
            'SUM(CASE WHEN nd.id = ', nd.id, ' THEN IFNULL(enp.days,   0) ELSE 0 END) AS `', nd.code, '_days`, ',
            'SUM(CASE WHEN nd.id = ', nd.id, ' THEN IFNULL(enp.amount, 0) ELSE 0 END) AS `', nd.code, '_amount`,',
            ' ''', REPLACE(REPLACE(nd.name, '''', ''''''), '`', ''), ''' AS `', nd.code, '_label`'
        ) ORDER BY nd.id SEPARATOR ', '
    ) INTO v_columns FROM nopay_days nd WHERE nd.is_active = 'Y' AND nd.id > 0;
    IF v_columns IS NULL OR v_columns = '' THEN
        SET v_columns = '0 AS no_active_np, 0 AS no_active_np_days, 0 AS no_active_np_amount, NULL AS no_active_np_label';
    END IF;
    SET v_sql = CONCAT(
        'SELECT e.id, e.employee_no, e.first_name, e.last_name, e.payroll_name, e.basic_salary, ',
        v_columns,
        ' FROM employee e
        LEFT JOIN emp_np enp ON e.id = enp.emp_id AND enp.payroll_month = ''', p_payroll_month, '''
        LEFT JOIN nopay_days nd ON enp.nopay_id = nd.id
        WHERE e.is_active = ''Y'' AND e.id > 0
        GROUP BY e.id, e.employee_no, e.first_name, e.last_name, e.payroll_name, e.basic_salary
        ORDER BY e.id'
    );
    SET @stmt = v_sql; PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;
END$$

DELIMITER ;
