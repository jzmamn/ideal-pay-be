ALTER TABLE variable_allowance
    ADD COLUMN IF NOT EXISTS is_taxable CHAR(1) NOT NULL DEFAULT 'N' AFTER is_active;

ALTER TABLE variable_deduction
    ADD COLUMN IF NOT EXISTS is_taxable CHAR(1) NOT NULL DEFAULT 'N' AFTER is_active;

ALTER TABLE emp_payroll_run
    ADD COLUMN IF NOT EXISTS taxable_earnings DECIMAL(15,2) NOT NULL DEFAULT 0.00 AFTER epf_liable_base;
