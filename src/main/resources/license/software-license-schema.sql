CREATE TABLE software_license (
  id BIGINT NOT NULL AUTO_INCREMENT,
  license_id VARCHAR(100) NOT NULL,
  customer_code VARCHAR(10) NOT NULL,
  customer_name VARCHAR(150) NOT NULL,
  plan VARCHAR(30) NOT NULL,
  employee_limit INT NOT NULL,
  valid_from DATE NOT NULL,
  valid_till DATE NOT NULL,
  maintenance_available CHAR(1) NOT NULL DEFAULT 'N',
  license_status VARCHAR(30) NOT NULL,
  raw_license_key MEDIUMTEXT NOT NULL,
  is_current CHAR(1) NOT NULL DEFAULT 'N',
  installed_by BIGINT NOT NULL,
  installed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by BIGINT NOT NULL,
  created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modified_by BIGINT NOT NULL,
  modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id), UNIQUE KEY uk_software_license_id (license_id),
  KEY idx_license_current (is_current), KEY idx_license_status (license_status),
  CONSTRAINT fk_license_installed_by FOREIGN KEY (installed_by) REFERENCES usr(id),
  CONSTRAINT fk_license_created_by FOREIGN KEY (created_by) REFERENCES usr(id),
  CONSTRAINT fk_license_modified_by FOREIGN KEY (modified_by) REFERENCES usr(id),
  CONSTRAINT chk_license_maintenance CHECK (maintenance_available IN ('Y','N')),
  CONSTRAINT chk_license_current CHECK (is_current IN ('Y','N'))
);

CREATE TABLE license_audit_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  license_id VARCHAR(100) NULL,
  action VARCHAR(50) NOT NULL,
  status VARCHAR(30) NOT NULL,
  message VARCHAR(1000) NULL,
  performed_by BIGINT NOT NULL,
  created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id), KEY idx_license_audit_license_id (license_id),
  CONSTRAINT fk_license_audit_user FOREIGN KEY (performed_by) REFERENCES usr(id)
);
