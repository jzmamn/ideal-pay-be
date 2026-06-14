ALTER TABLE bonus
    ADD COLUMN IF NOT EXISTS calculation_method VARCHAR(20) NOT NULL DEFAULT 'FIXED_AMOUNT'
        AFTER description;

UPDATE bonus
SET calculation_method = CASE
    WHEN formula_enabled = 'Y' THEN 'FORMULA_BASED'
    ELSE 'FIXED_AMOUNT'
END;

ALTER TABLE employee
    ADD COLUMN IF NOT EXISTS department_id BIGINT NULL AFTER designation_id;

ALTER TABLE employee
    ADD CONSTRAINT fk_employee_department
        FOREIGN KEY (department_id) REFERENCES department (id);

ALTER TABLE employee
    ADD INDEX idx_employee_department (department_id);

ALTER TABLE bonus_processing_batch
    ADD CONSTRAINT fk_bpb_processed_by
        FOREIGN KEY (processed_by) REFERENCES usr (id);
