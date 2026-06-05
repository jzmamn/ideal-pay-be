-- ============================================================
-- V5 — Email Config: create table (includes name column)
-- MySQL 8.0  |  Flyway migration
-- The email_config table was not covered by an earlier migration,
-- so we create it here with all columns, including the new `name` field.
-- ============================================================

CREATE TABLE IF NOT EXISTS email_config (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    name         VARCHAR(100)  NOT NULL                   COMMENT 'Human-readable label, e.g. Gmail, SendGrid',
    host         VARCHAR(255)  NOT NULL,
    port         INT           NOT NULL,
    username     VARCHAR(255)  NOT NULL,
    password     VARCHAR(500)  NOT NULL                   COMMENT 'Store encrypted in production',
    from_name    VARCHAR(150)  NOT NULL,
    from_address VARCHAR(255)  NOT NULL,
    use_tls      CHAR(1)       NOT NULL DEFAULT 'Y'       COMMENT 'Y/N',
    is_active    CHAR(1)       NOT NULL DEFAULT 'Y'       COMMENT 'Y/N',
    created_by   BIGINT        NOT NULL,
    created_date TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by  BIGINT        NOT NULL,
    modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_email_config         PRIMARY KEY (id),
    CONSTRAINT uk_email_config_name    UNIQUE      (name),
    CONSTRAINT chk_email_config_tls    CHECK       (use_tls   IN ('Y','N')),
    CONSTRAINT chk_email_config_active CHECK       (is_active IN ('Y','N')),
    CONSTRAINT fk_email_config_created FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_email_config_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- If the table already existed (created by Hibernate auto-DDL) and has rows,
-- add the name column and back-fill without failing on the CREATE above.
-- The IF NOT EXISTS on CREATE TABLE handles the already-exists case safely;
-- the name column will be missing in that scenario, so we add it conditionally
-- via the stored-procedure pattern used elsewhere in this project.

DROP PROCEDURE IF EXISTS v5_add_name_if_missing;
DELIMITER $$
CREATE PROCEDURE v5_add_name_if_missing()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'email_config'
          AND COLUMN_NAME  = 'name'
    ) THEN
        ALTER TABLE email_config
            ADD COLUMN name VARCHAR(100) NULL
                COMMENT 'Human-readable label, e.g. Gmail, SendGrid'
                AFTER id;

        UPDATE email_config SET name = CONCAT('Config-', id) WHERE name IS NULL OR name = '';

        ALTER TABLE email_config MODIFY COLUMN name VARCHAR(100) NOT NULL;

        ALTER TABLE email_config ADD UNIQUE INDEX uk_email_config_name (name);
    END IF;
END$$
DELIMITER ;

CALL v5_add_name_if_missing();
DROP PROCEDURE IF EXISTS v5_add_name_if_missing;
