-- Payroll-wide configurable values. Rows are maintained by database scripts;
-- the application only permits updating value and is_active.
CREATE TABLE system_setup (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    is_active CHAR(1) NOT NULL DEFAULT 'Y',
    effective_from DATE NOT NULL,
    effective_to DATE DEFAULT NULL,
    created_by BIGINT NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by BIGINT NOT NULL,
    modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_system_setup_code (code),
    CONSTRAINT chk_system_setup_active CHECK (is_active IN ('Y', 'N')),
    CONSTRAINT fk_system_setup_created_by FOREIGN KEY (created_by) REFERENCES usr(id),
    CONSTRAINT fk_system_setup_modified_by FOREIGN KEY (modified_by) REFERENCES usr(id)
);
