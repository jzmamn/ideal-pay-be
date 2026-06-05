-- ============================================================
--  Patch: add email_config_id to email_template
--  Run this if V6 migration did not execute (because V5 failed first).
-- ============================================================

ALTER TABLE email_template
    ADD COLUMN email_config_id BIGINT NULL
        COMMENT 'Optional: SMTP config used when sending this template'
        AFTER template_type;

ALTER TABLE email_template
    ADD CONSTRAINT fk_et_email_config
        FOREIGN KEY (email_config_id) REFERENCES email_config (id)
        ON DELETE SET NULL;
