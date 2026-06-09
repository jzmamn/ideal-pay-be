-- ============================================================
--  ideal_pay  —  Full Schema
--  MySQL 8.0  |  Single script covering all migrations (V2 – V6)
--  All V2 column additions and V6 FK are baked into the
--  CREATE TABLE statements; no separate ALTERs needed.
--  Safe to run on a fresh database.
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. ADMIN / AUTH
-- ============================================================

CREATE TABLE IF NOT EXISTS user_role (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    code          VARCHAR(10)  NULL UNIQUE,
    name          VARCHAR(50)  NOT NULL,
    is_active     CHAR(1)      NOT NULL DEFAULT 'Y',
    created_by    BIGINT       NOT NULL,
    created_date  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT       NOT NULL,
    modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- urol (permission role)
CREATE TABLE IF NOT EXISTS urol (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    code          VARCHAR(10)  NULL UNIQUE,
    name          VARCHAR(100) NOT NULL,
    is_active     CHAR(1)      NOT NULL DEFAULT 'Y',
    created_by    BIGINT       NOT NULL,
    created_date  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT       NOT NULL,
    modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- usr — self-referencing FKs added after creation
CREATE TABLE IF NOT EXISTS usr (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    code          VARCHAR(10)   NULL UNIQUE,
    name          VARCHAR(50)   NOT NULL,
    user_name     VARCHAR(50)   NOT NULL UNIQUE,
    email         VARCHAR(100)  NOT NULL UNIQUE,
    password      VARCHAR(255)  NOT NULL,
    role_id       BIGINT        NOT NULL,
    is_active     CHAR(1)       NOT NULL DEFAULT 'Y',
    created_by    BIGINT        NOT NULL,
    created_date  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT        NOT NULL,
    modified_date TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_usr_role FOREIGN KEY (role_id) REFERENCES user_role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- self-referencing audit FKs (added after table exists)
ALTER TABLE user_role
    ADD CONSTRAINT fk_user_role_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    ADD CONSTRAINT fk_user_role_modified FOREIGN KEY (modified_by) REFERENCES usr (id);

ALTER TABLE urol
    ADD CONSTRAINT fk_urol_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    ADD CONSTRAINT fk_urol_modified FOREIGN KEY (modified_by) REFERENCES usr (id);

CREATE TABLE IF NOT EXISTS grp (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    code          VARCHAR(10)  NULL UNIQUE,
    name          VARCHAR(100) NOT NULL,
    is_active     CHAR(1)      NOT NULL DEFAULT 'Y',
    created_by    BIGINT       NOT NULL,
    created_date  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT       NOT NULL,
    modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_grp_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_grp_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS usr_grp (
    id            BIGINT    NOT NULL AUTO_INCREMENT,
    USER_id       BIGINT    NOT NULL,
    GRP_id        BIGINT    NOT NULL,
    is_active     CHAR(1)   NOT NULL DEFAULT 'Y',
    created_by    BIGINT    NOT NULL,
    created_date  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT    NOT NULL,
    modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_usr_grp_user     FOREIGN KEY (USER_id)    REFERENCES usr (id),
    CONSTRAINT fk_usr_grp_grp      FOREIGN KEY (GRP_id)     REFERENCES grp (id),
    CONSTRAINT fk_usr_grp_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_usr_grp_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS usr_urol (
    id            BIGINT    NOT NULL AUTO_INCREMENT,
    USER_id       BIGINT    NOT NULL,
    UROL_id       BIGINT    NOT NULL,
    is_active     CHAR(1)   NOT NULL DEFAULT 'Y',
    created_by    BIGINT    NOT NULL,
    created_date  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT    NOT NULL,
    modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_usr_urol_user     FOREIGN KEY (USER_id)    REFERENCES usr  (id),
    CONSTRAINT fk_usr_urol_urol     FOREIGN KEY (UROL_id)    REFERENCES urol (id),
    CONSTRAINT fk_usr_urol_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_usr_urol_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. REFERENCE / LOOKUP TABLES
-- ============================================================

CREATE TABLE IF NOT EXISTS status (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    code          VARCHAR(10)  NULL UNIQUE,
    name          VARCHAR(150) NOT NULL,
    description   VARCHAR(255) NOT NULL,
    date_only     CHAR(1)      NOT NULL DEFAULT 'N',
    is_active     CHAR(1)      NOT NULL DEFAULT 'Y',
    created_by    BIGINT       NOT NULL,
    created_date  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT       NOT NULL,
    modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_status_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_status_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS type (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    code          VARCHAR(10)  NULL UNIQUE,
    name          VARCHAR(150) NOT NULL,
    description   VARCHAR(255) NOT NULL,
    is_active     CHAR(1)      NOT NULL DEFAULT 'Y',
    is_date_range CHAR(1)      NOT NULL DEFAULT 'N',
    created_by    BIGINT       NOT NULL,
    created_date  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT       NOT NULL,
    modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_type_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_type_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS country (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    code          VARCHAR(10)  NULL UNIQUE,
    name          VARCHAR(70)  NOT NULL,
    is_active     CHAR(1)      NOT NULL DEFAULT 'Y',
    iso2          VARCHAR(2)   NOT NULL UNIQUE,
    iso3          VARCHAR(3)   NOT NULL,
    phone_code    BIGINT       NOT NULL,
    created_by    BIGINT       NOT NULL,
    created_date  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT       NOT NULL,
    modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_country_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_country_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS district (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    code          VARCHAR(10)  NULL UNIQUE,
    name          VARCHAR(70)  NOT NULL,
    is_active     CHAR(1)      NOT NULL DEFAULT 'Y',
    created_by    BIGINT       NOT NULL,
    created_date  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT       NOT NULL,
    modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_district_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_district_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. COMPANY STRUCTURE
-- ============================================================

CREATE TABLE IF NOT EXISTS company (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    code          VARCHAR(10)  NULL UNIQUE,
    name          VARCHAR(150) NOT NULL,
    contact_person VARCHAR(150) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255) NULL,
    city          VARCHAR(100) NOT NULL,
    address_email VARCHAR(150) NULL,
    telephone     VARCHAR(20)  NOT NULL,
    fax           VARCHAR(20)  NULL,
    email         VARCHAR(150) NULL,
    logo          VARCHAR(500) NULL,
    is_active     CHAR(1)      NOT NULL DEFAULT 'Y',
    created_by    BIGINT       NOT NULL,
    created_date  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT       NOT NULL,
    modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_company_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_company_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS branch (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    code          VARCHAR(10)  NULL UNIQUE,
    name          VARCHAR(150) NOT NULL,
    description   VARCHAR(255) NOT NULL,
    location      VARCHAR(255) NOT NULL,
    is_active     CHAR(1)      NOT NULL DEFAULT 'Y',
    created_by    BIGINT       NOT NULL,
    created_date  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT       NOT NULL,
    modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_branch_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_branch_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS department (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    code          VARCHAR(10)  NULL UNIQUE,
    name          VARCHAR(150) NOT NULL,
    description   VARCHAR(255) NOT NULL,
    is_active     CHAR(1)      NOT NULL DEFAULT 'Y',
    created_by    BIGINT       NOT NULL,
    created_date  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT       NOT NULL,
    modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_department_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_department_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS designation (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    code          VARCHAR(10)  NULL UNIQUE,
    name          VARCHAR(150) NOT NULL,
    description   VARCHAR(255) NOT NULL,
    is_active     CHAR(1)      NOT NULL DEFAULT 'Y',
    created_by    BIGINT       NOT NULL,
    created_date  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT       NOT NULL,
    modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_designation_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_designation_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS grade (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    code          VARCHAR(10)     NULL UNIQUE,
    name          VARCHAR(150)    NOT NULL,
    amount        DECIMAL(10,2)   NULL,
    description   VARCHAR(255)    NULL,
    is_active     CHAR(1)         NOT NULL DEFAULT 'Y',
    created_by    BIGINT          NOT NULL,
    created_date  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT          NOT NULL,
    modified_date TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_grade_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_grade_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS job_category (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    code          VARCHAR(10)  NULL UNIQUE,
    name          VARCHAR(150) NOT NULL,
    description   VARCHAR(255) NOT NULL,
    is_active     CHAR(1)      NOT NULL DEFAULT 'Y',
    created_by    BIGINT       NOT NULL,
    created_date  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT       NOT NULL,
    modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_job_cat_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_job_cat_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. BANKING
-- ============================================================

CREATE TABLE IF NOT EXISTS bank (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    code          VARCHAR(20)  NOT NULL UNIQUE,
    name          VARCHAR(150) NOT NULL,
    is_active     CHAR(1)      NOT NULL DEFAULT 'Y',
    created_by    BIGINT       NOT NULL,
    created_date  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT       NOT NULL,
    modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_bank_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_bank_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS bank_branch (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    bank_code     VARCHAR(20)  NOT NULL,
    branch_code   VARCHAR(20)  NOT NULL,
    branch_name   VARCHAR(150) NOT NULL,
    is_active     CHAR(1)      NOT NULL DEFAULT 'Y',
    created_by    BIGINT       NOT NULL,
    created_date  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT       NOT NULL,
    modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_bank_branch_bank     FOREIGN KEY (bank_code)   REFERENCES bank (code),
    CONSTRAINT fk_bank_branch_created  FOREIGN KEY (created_by)  REFERENCES usr  (id),
    CONSTRAINT fk_bank_branch_modified FOREIGN KEY (modified_by) REFERENCES usr  (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 5. PAYROLL COMPONENTS
-- ============================================================

CREATE TABLE IF NOT EXISTS fixed_allowance (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    code             VARCHAR(10)   NULL UNIQUE,
    name             VARCHAR(150)  NOT NULL,
    description      VARCHAR(255)  NULL,
    amount           DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    is_active        CHAR(1)       NOT NULL DEFAULT 'Y',
    is_taxable       CHAR(1)       NOT NULL DEFAULT 'N',
    liable_for_epf   CHAR(1)       NOT NULL DEFAULT 'Y',
    liable_for_etf   CHAR(1)       NOT NULL DEFAULT 'Y',
    liable_for_paye  CHAR(1)       NOT NULL DEFAULT 'Y',
    liable_no_pay    CHAR(1)       NOT NULL DEFAULT 'Y',
    formula          VARCHAR(500)  NULL,
    formula_enabled  CHAR(1)       NOT NULL DEFAULT 'N',
    created_by       BIGINT        NOT NULL,
    created_date     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by      BIGINT        NOT NULL,
    modified_date    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_fa_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_fa_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS variable_allowance (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    code             VARCHAR(10)   NULL UNIQUE,
    name             VARCHAR(50)   NOT NULL,
    description      VARCHAR(255)  NULL,
    is_active        CHAR(1)       NOT NULL DEFAULT 'Y',
    liable_for_epf   CHAR(1)       NOT NULL DEFAULT 'Y',
    liable_for_etf   CHAR(1)       NOT NULL DEFAULT 'Y',
    liable_for_paye  CHAR(1)       NOT NULL DEFAULT 'Y',
    liable_no_pay    CHAR(1)       NOT NULL DEFAULT 'Y',
    created_by       BIGINT        NOT NULL,
    created_date     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by      BIGINT        NOT NULL,
    modified_date    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_va_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_va_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS fixed_deduction (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    code             VARCHAR(10)   NULL UNIQUE,
    name             VARCHAR(50)   NOT NULL,
    description      VARCHAR(255)  NULL,
    amount           DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    is_active        CHAR(1)       NOT NULL DEFAULT 'Y',
    liable_for_epf   CHAR(1)       NOT NULL DEFAULT 'Y',
    liable_for_etf   CHAR(1)       NOT NULL DEFAULT 'Y',
    liable_for_paye  CHAR(1)       NOT NULL DEFAULT 'Y',
    liable_no_pay    CHAR(1)       NOT NULL DEFAULT 'Y',
    formula          VARCHAR(500)  NULL,
    formula_enabled  CHAR(1)       NOT NULL DEFAULT 'N',
    created_by       BIGINT        NOT NULL,
    created_date     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by      BIGINT        NOT NULL,
    modified_date    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_fd_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_fd_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS variable_deduction (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    code             VARCHAR(10)   NULL UNIQUE,
    name             VARCHAR(50)   NOT NULL,
    description      VARCHAR(255)  NULL,
    is_active        CHAR(1)       NOT NULL DEFAULT 'Y',
    liable_for_epf   CHAR(1)       NOT NULL DEFAULT 'Y',
    liable_for_etf   CHAR(1)       NOT NULL DEFAULT 'Y',
    liable_for_paye  CHAR(1)       NOT NULL DEFAULT 'Y',
    liable_no_pay    CHAR(1)       NOT NULL DEFAULT 'Y',
    created_by       BIGINT        NOT NULL,
    created_date     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by      BIGINT        NOT NULL,
    modified_date    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_vd_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_vd_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS loan (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    code          VARCHAR(10)   NOT NULL UNIQUE,
    name          VARCHAR(150)  NOT NULL,
    description   VARCHAR(255)  NOT NULL,
    amount        DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    is_active     CHAR(1)       NOT NULL DEFAULT 'Y',
    created_by    BIGINT        NOT NULL,
    created_date  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT        NOT NULL,
    modified_date TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_loan_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_loan_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- V2: liable_no_pay, formula, formula_enabled baked in
CREATE TABLE IF NOT EXISTS nopay_days (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    code             VARCHAR(10)   NULL UNIQUE,
    name             VARCHAR(150)  NOT NULL,
    days             DECIMAL(5,2)  NOT NULL DEFAULT 0.00,
    description      VARCHAR(255)  NOT NULL,
    is_active        CHAR(1)       NOT NULL DEFAULT 'Y',
    liable_no_pay    CHAR(1)       NOT NULL DEFAULT 'Y'  COMMENT 'Y = nopay deducted from EPF base',
    formula          VARCHAR(500)  NULL                  COMMENT 'MVEL expression',
    formula_enabled  CHAR(1)       NOT NULL DEFAULT 'N'  COMMENT 'Y = evaluate formula at run time',
    created_by       BIGINT        NOT NULL,
    created_date     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by      BIGINT        NOT NULL,
    modified_date    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_nopay_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_nopay_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- V2: liable_for_epf, liable_for_etf, liable_for_paye baked in
CREATE TABLE IF NOT EXISTS overtime (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    code             VARCHAR(10)   NULL UNIQUE,
    name             VARCHAR(150)  NOT NULL,
    description      VARCHAR(255)  NOT NULL,
    is_active        CHAR(1)       NOT NULL DEFAULT 'Y',
    formula          VARCHAR(500)  NULL,
    formula_enabled  CHAR(1)       NOT NULL DEFAULT 'N',
    liable_for_epf   CHAR(1)       NOT NULL DEFAULT 'Y'  COMMENT 'Y = OT included in EPF base',
    liable_for_etf   CHAR(1)       NOT NULL DEFAULT 'Y'  COMMENT 'Y = OT included in ETF base',
    liable_for_paye  CHAR(1)       NOT NULL DEFAULT 'Y'  COMMENT 'Y = OT included in PAYE base',
    created_by       BIGINT        NOT NULL,
    created_date     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by      BIGINT        NOT NULL,
    modified_date    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_ot_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_ot_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- V2: working_days baked in
CREATE TABLE IF NOT EXISTS payroll_period (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    period_month  VARCHAR(7)   NOT NULL,
    working_days  INT          NOT NULL DEFAULT 26     COMMENT 'Payable days in this period',
    status        VARCHAR(10)  NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN | CLOSED',
    closed_date   TIMESTAMP    NULL,
    closed_by     BIGINT       NULL,
    created_by    BIGINT       NOT NULL,
    created_date  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT       NOT NULL,
    modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_payroll_period_month  UNIQUE      (period_month),
    CONSTRAINT chk_period_status        CHECK       (status IN ('OPEN','CLOSED')),
    CONSTRAINT fk_period_closed_by      FOREIGN KEY (closed_by)   REFERENCES usr (id),
    CONSTRAINT fk_period_created        FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_period_modified       FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 6. EMPLOYEE
-- ============================================================

CREATE TABLE IF NOT EXISTS employee (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    employee_no         VARCHAR(20)   NOT NULL UNIQUE,
    first_name          VARCHAR(100)  NOT NULL,
    last_name           VARCHAR(100)  NOT NULL,
    date_of_birth       DATE          NULL,
    nic                 VARCHAR(15)   NULL,
    is_active           CHAR(1)       NOT NULL DEFAULT 'Y',
    remarks             VARCHAR(500)  NULL,
    payroll_name        VARCHAR(150)  NOT NULL,
    epf_no              VARCHAR(50)   NULL,
    etf_no              VARCHAR(50)   NULL,
    basic_salary        DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    bank_id             BIGINT        NULL,
    bank_branch_id      BIGINT        NULL,
    account_no          VARCHAR(50)   NULL,
    joined_date         DATE          NOT NULL,
    employee_type_id    BIGINT        NOT NULL,
    cotract_from        DATE          NULL,
    contract_to         DATE          NULL,
    nopay_days_id       BIGINT        NOT NULL,
    job_category_id     BIGINT        NOT NULL,
    designation_id      BIGINT        NOT NULL,
    branch_id           BIGINT        NOT NULL,
    grade_id            BIGINT        NOT NULL,
    status_id           BIGINT        NOT NULL,
    stat_date           DATE          NULL,
    stat_from           DATE          NULL,
    stat_to             DATE          NULL,
    phone               VARCHAR(20)   NULL,
    email               VARCHAR(150)  NULL,
    adrs_line1          VARCHAR(255)  NULL,
    adrs_line2          VARCHAR(255)  NULL,
    city                VARCHAR(100)  NULL,
    district            VARCHAR(100)  NULL,
    country_id          BIGINT        NOT NULL,
    contact_person      VARCHAR(150)  NULL,
    cp_address          VARCHAR(255)  NULL,
    cp_contact_number   VARCHAR(20)   NULL,
    created_by          BIGINT        NOT NULL,
    created_date        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by         BIGINT        NOT NULL,
    modified_date       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_emp_type        FOREIGN KEY (employee_type_id) REFERENCES type        (id),
    CONSTRAINT fk_emp_nopay       FOREIGN KEY (nopay_days_id)    REFERENCES nopay_days  (id),
    CONSTRAINT fk_emp_job_cat     FOREIGN KEY (job_category_id)  REFERENCES job_category(id),
    CONSTRAINT fk_emp_designation FOREIGN KEY (designation_id)   REFERENCES designation (id),
    CONSTRAINT fk_emp_branch      FOREIGN KEY (branch_id)        REFERENCES branch      (id),
    CONSTRAINT fk_emp_grade       FOREIGN KEY (grade_id)         REFERENCES grade       (id),
    CONSTRAINT fk_emp_status      FOREIGN KEY (status_id)        REFERENCES status      (id),
    CONSTRAINT fk_emp_country     FOREIGN KEY (country_id)       REFERENCES country     (id),
    CONSTRAINT fk_emp_bank        FOREIGN KEY (bank_id)          REFERENCES bank        (id),
    CONSTRAINT fk_emp_bank_branch FOREIGN KEY (bank_branch_id)   REFERENCES bank_branch (id),
    CONSTRAINT fk_emp_created     FOREIGN KEY (created_by)       REFERENCES usr         (id),
    CONSTRAINT fk_emp_modified    FOREIGN KEY (modified_by)      REFERENCES usr         (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 7. PAYROLL RUN  (V2 EPF/ETF/PAYE columns baked in)
-- ============================================================

CREATE TABLE IF NOT EXISTS emp_payroll_run (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    emp_id            BIGINT        NOT NULL,
    payroll_month     VARCHAR(20)   NOT NULL,
    basic_salary      DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_allowances  DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_deductions  DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    gross_pay         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    net_pay           DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    epf_liable_base   DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'Base for EPF/ETF computation',
    employee_epf      DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'Employee EPF 8%',
    employer_epf      DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'Employer EPF 12%',
    etf               DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'ETF 3%',
    paye_tax          DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'PAYE income tax',
    working_days      INT           NOT NULL DEFAULT 26   COMMENT 'Working days used for this run',
    status            VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    run_type          VARCHAR(20)   NOT NULL DEFAULT 'NORMAL',
    parent_run_id     BIGINT        NULL,
    processed_date    TIMESTAMP     NULL,
    processed_by      BIGINT        NULL,
    created_by        BIGINT        NOT NULL,
    created_date      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by       BIGINT        NOT NULL,
    modified_date     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_emp_payroll_run   UNIQUE (emp_id, payroll_month),
    CONSTRAINT chk_run_status       CHECK  (status   IN ('DRAFT','PROCESSED','LOCKED','CORRECTION_DRAFT','CORRECTION_LOCKED')),
    CONSTRAINT chk_run_type         CHECK  (run_type IN ('NORMAL','CORRECTION')),
    CONSTRAINT fk_run_emp           FOREIGN KEY (emp_id)       REFERENCES employee (id),
    CONSTRAINT fk_run_processed_by  FOREIGN KEY (processed_by) REFERENCES usr      (id),
    CONSTRAINT fk_run_created       FOREIGN KEY (created_by)   REFERENCES usr      (id),
    CONSTRAINT fk_run_modified      FOREIGN KEY (modified_by)  REFERENCES usr      (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- V2: component_id nullable, chk_detail_component_type extended
CREATE TABLE IF NOT EXISTS emp_payroll_run_detail (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    run_id           BIGINT        NOT NULL,
    component_type   VARCHAR(10)   NOT NULL,
    component_id     BIGINT        NULL,
    component_code   VARCHAR(20)   NOT NULL,
    component_name   VARCHAR(150)  NOT NULL,
    amount           DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    hours            DECIMAL(5,2)  NULL,
    days             DECIMAL(5,2)  NULL,
    created_by       BIGINT        NOT NULL,
    created_date     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by      BIGINT        NOT NULL,
    modified_date    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT chk_detail_component_type CHECK (component_type IN ('FA','FD','VA','VD','OT','NOPAY','EPF_EE','EPF_ER','ETF','PAYE','SA')),
    CONSTRAINT fk_detail_run      FOREIGN KEY (run_id)      REFERENCES emp_payroll_run (id),
    CONSTRAINT fk_detail_created  FOREIGN KEY (created_by)  REFERENCES usr             (id),
    CONSTRAINT fk_detail_modified FOREIGN KEY (modified_by) REFERENCES usr             (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 8. EMPLOYEE PAYROLL ENTRIES
-- ============================================================

CREATE TABLE IF NOT EXISTS emp_fa (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    emp_id         BIGINT        NOT NULL,
    fa_id          BIGINT        NOT NULL,
    amount         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    payroll_month  VARCHAR(20)   NOT NULL,
    is_processed   CHAR(1)       NOT NULL DEFAULT 'N',
    processed_date TIMESTAMP     NULL,
    created_by     BIGINT        NOT NULL,
    created_date   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by    BIGINT        NOT NULL,
    modified_date  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_emp_fa_emp      FOREIGN KEY (emp_id)      REFERENCES employee        (id),
    CONSTRAINT fk_emp_fa_fa       FOREIGN KEY (fa_id)       REFERENCES fixed_allowance (id),
    CONSTRAINT fk_emp_fa_created  FOREIGN KEY (created_by)  REFERENCES usr             (id),
    CONSTRAINT fk_emp_fa_modified FOREIGN KEY (modified_by) REFERENCES usr             (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS emp_fd (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    emp_id         BIGINT        NOT NULL,
    fd_id          BIGINT        NOT NULL,
    amount         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    payroll_month  VARCHAR(20)   NOT NULL,
    is_processed   TINYINT(1)    NOT NULL DEFAULT 0,
    processed_date TIMESTAMP     NULL,
    created_by     BIGINT        NOT NULL,
    created_date   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by    BIGINT        NOT NULL,
    modified_date  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_emp_fd_emp      FOREIGN KEY (emp_id)      REFERENCES employee        (id),
    CONSTRAINT fk_emp_fd_fd       FOREIGN KEY (fd_id)       REFERENCES fixed_deduction (id),
    CONSTRAINT fk_emp_fd_created  FOREIGN KEY (created_by)  REFERENCES usr             (id),
    CONSTRAINT fk_emp_fd_modified FOREIGN KEY (modified_by) REFERENCES usr             (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS emp_va (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    emp_id         BIGINT        NOT NULL,
    va_id          BIGINT        NOT NULL,
    amount         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    payroll_month  VARCHAR(20)   NULL,
    is_processed   CHAR(1)       NOT NULL DEFAULT 'N',
    processed_date TIMESTAMP     NULL,
    created_by     BIGINT        NOT NULL,
    created_date   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by    BIGINT        NOT NULL,
    modified_date  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_emp_va_emp      FOREIGN KEY (emp_id)      REFERENCES employee          (id),
    CONSTRAINT fk_emp_va_va       FOREIGN KEY (va_id)       REFERENCES variable_allowance(id),
    CONSTRAINT fk_emp_va_created  FOREIGN KEY (created_by)  REFERENCES usr               (id),
    CONSTRAINT fk_emp_va_modified FOREIGN KEY (modified_by) REFERENCES usr               (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS emp_vd (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    emp_id         BIGINT        NOT NULL,
    vd_id          BIGINT        NOT NULL,
    amount         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    payroll_month  VARCHAR(20)   NULL,
    is_processed   CHAR(1)       NOT NULL DEFAULT 'N',
    processed_date TIMESTAMP     NULL,
    created_by     BIGINT        NOT NULL,
    created_date   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by    BIGINT        NOT NULL,
    modified_date  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_emp_vd_emp      FOREIGN KEY (emp_id)      REFERENCES employee           (id),
    CONSTRAINT fk_emp_vd_vd       FOREIGN KEY (vd_id)       REFERENCES variable_deduction (id),
    CONSTRAINT fk_emp_vd_created  FOREIGN KEY (created_by)  REFERENCES usr                (id),
    CONSTRAINT fk_emp_vd_modified FOREIGN KEY (modified_by) REFERENCES usr                (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS emp_ot (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    emp_id         BIGINT        NOT NULL,
    overtime_id    BIGINT        NOT NULL,
    hours          DECIMAL(5,2)  NOT NULL DEFAULT 0.00,
    amount         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    payroll_month  VARCHAR(20)   NULL,
    is_processed   CHAR(1)       NOT NULL DEFAULT 'N',
    processed_date TIMESTAMP     NULL,
    created_by     BIGINT        NOT NULL,
    created_date   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by    BIGINT        NOT NULL,
    modified_date  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_emp_ot_emp      FOREIGN KEY (emp_id)      REFERENCES employee (id),
    CONSTRAINT fk_emp_ot_ot       FOREIGN KEY (overtime_id) REFERENCES overtime (id),
    CONSTRAINT fk_emp_ot_created  FOREIGN KEY (created_by)  REFERENCES usr      (id),
    CONSTRAINT fk_emp_ot_modified FOREIGN KEY (modified_by) REFERENCES usr      (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS emp_np (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    emp_id         BIGINT        NOT NULL,
    nopay_id       BIGINT        NOT NULL,
    days           DECIMAL(5,2)  NOT NULL DEFAULT 0.00,
    amount         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    payroll_month  VARCHAR(20)   NULL,
    is_processed   CHAR(1)       NOT NULL DEFAULT 'N',
    processed_date TIMESTAMP     NULL,
    created_by     BIGINT        NOT NULL,
    created_date   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by    BIGINT        NOT NULL,
    modified_date  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_emp_np_emp      FOREIGN KEY (emp_id)    REFERENCES employee   (id),
    CONSTRAINT fk_emp_np_nopay    FOREIGN KEY (nopay_id)  REFERENCES nopay_days (id),
    CONSTRAINT fk_emp_np_created  FOREIGN KEY (created_by)  REFERENCES usr      (id),
    CONSTRAINT fk_emp_np_modified FOREIGN KEY (modified_by) REFERENCES usr      (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS emp_sal_adv (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    emp_id         BIGINT        NOT NULL,
    amount         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    payroll_month  VARCHAR(20)   NOT NULL,
    is_processed   CHAR(1)       NOT NULL DEFAULT 'N',
    processed_date TIMESTAMP     NULL,
    created_by     BIGINT        NOT NULL,
    created_date   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by    BIGINT        NOT NULL,
    modified_date  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_sal_adv_emp      FOREIGN KEY (emp_id)      REFERENCES employee (id),
    CONSTRAINT fk_sal_adv_created  FOREIGN KEY (created_by)  REFERENCES usr      (id),
    CONSTRAINT fk_sal_adv_modified FOREIGN KEY (modified_by) REFERENCES usr      (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS emp_loan (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    emp_id         BIGINT        NOT NULL,
    loan_id        BIGINT        NOT NULL,
    amount         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    payroll_month  VARCHAR(20)   NOT NULL,
    is_processed   CHAR(1)       NOT NULL DEFAULT 'N',
    processed_date TIMESTAMP     NULL,
    created_by     BIGINT        NOT NULL,
    created_date   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by    BIGINT        NOT NULL,
    modified_date  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_emp_loan_emp      FOREIGN KEY (emp_id)      REFERENCES employee (id),
    CONSTRAINT fk_emp_loan_loan     FOREIGN KEY (loan_id)     REFERENCES loan     (id),
    CONSTRAINT fk_emp_loan_created  FOREIGN KEY (created_by)  REFERENCES usr      (id),
    CONSTRAINT fk_emp_loan_modified FOREIGN KEY (modified_by) REFERENCES usr      (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS emp_sal_incr (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    emp_id         BIGINT        NOT NULL,
    amount         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    payroll_month  VARCHAR(20)   NOT NULL,
    is_processed   CHAR(1)       NOT NULL DEFAULT 'N',
    processed_date TIMESTAMP     NULL,
    created_by     BIGINT        NOT NULL,
    created_date   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by    BIGINT        NOT NULL,
    modified_date  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_sal_incr_emp      FOREIGN KEY (emp_id)      REFERENCES employee (id),
    CONSTRAINT fk_sal_incr_created  FOREIGN KEY (created_by)  REFERENCES usr      (id),
    CONSTRAINT fk_sal_incr_modified FOREIGN KEY (modified_by) REFERENCES usr      (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS bonus (
    id                          BIGINT          NOT NULL AUTO_INCREMENT,
    code                        VARCHAR(10)     NULL     UNIQUE,
    name                        VARCHAR(150)    NOT NULL,
    description                 VARCHAR(255)    NULL,
    calculation_type            VARCHAR(30)     NOT NULL COMMENT 'FIXED_AMOUNT | PERCENTAGE_OF_BASIC | PERCENTAGE_OF_ANNUAL | PERFORMANCE_RATING | PROFIT_BASED | KPI_BASED | PRO_RATED | ATTENDANCE_BASED',
    fixed_amount                DECIMAL(15,2)   NULL,
    percentage_rate             DECIMAL(7,4)    NULL,
    target_bonus                DECIMAL(15,2)   NULL,
    rating_multipliers          VARCHAR(1000)   NULL     COMMENT 'JSON array: [{rating,label,multiplierPct}]',
    profit_allocation_pct       DECIMAL(7,4)    NULL,
    weightage_pct               DECIMAL(7,4)    NULL,
    kpi_definitions             VARCHAR(1000)   NULL     COMMENT 'JSON array: [{name,weightPct,achievementPct}]',
    pro_rate_full_bonus         DECIMAL(15,2)   NULL,
    attendance_max_bonus        DECIMAL(15,2)   NULL,
    attendance_penalty_per_day  DECIMAL(15,2)   NULL,
    is_active                   CHAR(1)         NOT NULL DEFAULT 'Y',
    created_by                  BIGINT          NOT NULL,
    created_date                TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by                 BIGINT          NOT NULL,
    modified_date               TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_bonus_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_bonus_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS emp_bonus (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    emp_id         BIGINT        NOT NULL,
    bonus_id       BIGINT        NULL,
    amount         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    payroll_month  VARCHAR(20)   NOT NULL,
    is_processed   CHAR(1)       NOT NULL DEFAULT 'N',
    processed_date TIMESTAMP     NULL,
    created_by     BIGINT        NOT NULL,
    created_date   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by    BIGINT        NOT NULL,
    modified_date  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_emp_bonus_emp     FOREIGN KEY (emp_id)    REFERENCES employee (id),
    CONSTRAINT fk_emp_bonus_bonus   FOREIGN KEY (bonus_id)  REFERENCES bonus    (id),
    CONSTRAINT fk_emp_bonus_created  FOREIGN KEY (created_by)  REFERENCES usr   (id),
    CONSTRAINT fk_emp_bonus_modified FOREIGN KEY (modified_by) REFERENCES usr   (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 9. SALARY INCREMENT  (V_salary_increment migration)
-- ============================================================

CREATE TABLE IF NOT EXISTS salary_increment (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    code             VARCHAR(20)   NOT NULL UNIQUE,
    name             VARCHAR(150)  NOT NULL,
    type             VARCHAR(20)   NOT NULL  COMMENT 'PERCENTAGE | FIXED_AMOUNT | NEW_BASIC',
    effective_month  VARCHAR(20)   NOT NULL,
    status           VARCHAR(20)   NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT | APPROVED | APPLIED',
    remarks          VARCHAR(500)  NULL,
    created_by       BIGINT        NOT NULL,
    created_date     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by      BIGINT        NOT NULL,
    modified_date    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_si_created  FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_si_modified FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS salary_increment_detail (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    increment_id     BIGINT        NOT NULL,
    emp_id           BIGINT        NOT NULL,
    current_basic    DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    increment_basic  DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    new_basic        DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    is_exported      CHAR(1)       NOT NULL DEFAULT 'N',
    exported_date    TIMESTAMP     NULL,
    remarks          VARCHAR(500)  NULL,
    created_by       BIGINT        NOT NULL,
    created_date     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by      BIGINT        NOT NULL,
    modified_date    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_si_detail      UNIQUE      (increment_id, emp_id),
    CONSTRAINT fk_si_det_incr    FOREIGN KEY (increment_id) REFERENCES salary_increment (id),
    CONSTRAINT fk_si_det_emp     FOREIGN KEY (emp_id)       REFERENCES employee         (id),
    CONSTRAINT fk_si_det_created  FOREIGN KEY (created_by)  REFERENCES usr              (id),
    CONSTRAINT fk_si_det_modified FOREIGN KEY (modified_by) REFERENCES usr              (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS salary_increment_fa (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    detail_id        BIGINT        NOT NULL,
    fa_id            BIGINT        NOT NULL,
    current_amount   DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    increment_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    new_amount       DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    created_by       BIGINT        NOT NULL,
    created_date     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by      BIGINT        NOT NULL,
    modified_date    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_si_fa          UNIQUE      (detail_id, fa_id),
    CONSTRAINT fk_si_fa_detail   FOREIGN KEY (detail_id)   REFERENCES salary_increment_detail (id),
    CONSTRAINT fk_si_fa_fa       FOREIGN KEY (fa_id)       REFERENCES fixed_allowance         (id),
    CONSTRAINT fk_si_fa_created  FOREIGN KEY (created_by)  REFERENCES usr                     (id),
    CONSTRAINT fk_si_fa_modified FOREIGN KEY (modified_by) REFERENCES usr                     (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 10. BANK TRANSFER  (V3)
-- ============================================================

CREATE TABLE IF NOT EXISTS bank_transfer_template (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    bank_id          BIGINT        NOT NULL,
    bank_code        VARCHAR(20)   NOT NULL,
    bank_name        VARCHAR(100)  NOT NULL,
    header_template  TEXT          NULL     COMMENT 'Supports {{date}} {{bank_name}} {{bank_code}} {{record_count}} {{total_amount}}',
    detail_template  TEXT          NOT NULL COMMENT 'Supports {{employee_no}} {{name}} {{account_no}} {{bank_code}} {{branch_code}} {{amount}} {{date}}',
    footer_template  TEXT          NULL     COMMENT 'Supports {{record_count}} {{total_amount}} {{date}}',
    file_extension   VARCHAR(10)   NOT NULL DEFAULT 'txt',
    created_by       BIGINT        NOT NULL,
    created_date     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by      BIGINT        NOT NULL,
    modified_date    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_bank_transfer_template  UNIQUE      (bank_id),
    CONSTRAINT fk_btt_bank                FOREIGN KEY (bank_id)     REFERENCES bank (id),
    CONSTRAINT fk_btt_created             FOREIGN KEY (created_by)  REFERENCES usr  (id),
    CONSTRAINT fk_btt_modified            FOREIGN KEY (modified_by) REFERENCES usr  (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS emp_transfer_log (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    payroll_run_id      BIGINT        NOT NULL,
    transfer_type       VARCHAR(20)   NOT NULL COMMENT 'SALARY | SALARY_ADVANCE | FIXED_ALLOWANCE',
    bank_id             BIGINT        NULL,
    bank_code           VARCHAR(20)   NULL,
    transferred_amount  DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    transferred_date    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    transferred_by      BIGINT        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_emp_transfer_log    UNIQUE (payroll_run_id, transfer_type),
    CONSTRAINT chk_etl_transfer_type  CHECK  (transfer_type IN ('SALARY','SALARY_ADVANCE','FIXED_ALLOWANCE')),
    CONSTRAINT fk_etl_payroll_run     FOREIGN KEY (payroll_run_id) REFERENCES emp_payroll_run (id),
    CONSTRAINT fk_etl_bank            FOREIGN KEY (bank_id)        REFERENCES bank            (id),
    CONSTRAINT fk_etl_transferred_by  FOREIGN KEY (transferred_by) REFERENCES usr             (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 11. EMAIL CONFIG  (V5)
-- ============================================================

CREATE TABLE IF NOT EXISTS email_config (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    name          VARCHAR(100)  NOT NULL                   COMMENT 'Human-readable label, e.g. Gmail, SendGrid',
    host          VARCHAR(255)  NOT NULL,
    port          INT           NOT NULL,
    username      VARCHAR(255)  NOT NULL,
    password      VARCHAR(500)  NOT NULL                   COMMENT 'Store encrypted in production',
    from_name     VARCHAR(150)  NOT NULL,
    from_address  VARCHAR(255)  NOT NULL,
    use_tls       CHAR(1)       NOT NULL DEFAULT 'Y'       COMMENT 'Y/N',
    is_active     CHAR(1)       NOT NULL DEFAULT 'Y'       COMMENT 'Y/N',
    created_by    BIGINT        NOT NULL,
    created_date  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by   BIGINT        NOT NULL,
    modified_date TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_email_config_name     UNIQUE (name),
    CONSTRAINT chk_email_config_tls     CHECK  (use_tls   IN ('Y','N')),
    CONSTRAINT chk_email_config_active  CHECK  (is_active IN ('Y','N')),
    CONSTRAINT fk_email_cfg_created     FOREIGN KEY (created_by)  REFERENCES usr (id),
    CONSTRAINT fk_email_cfg_modified    FOREIGN KEY (modified_by) REFERENCES usr (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 12. EMAIL TEMPLATE  (V4 + V6)
-- ============================================================

CREATE TABLE IF NOT EXISTS email_template (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    name             VARCHAR(150)  NOT NULL                    COMMENT 'Human-readable template name',
    template_type    VARCHAR(30)   NOT NULL                    COMMENT 'PAYSLIP | SALARY_ADVANCE | SALARY_INCREMENT | GENERAL',
    email_config_id  BIGINT        NULL                        COMMENT 'Optional SMTP config — NULL = use active config',
    subject          VARCHAR(500)  NOT NULL                    COMMENT 'Email subject — supports {{variables}}',
    body             LONGTEXT      NOT NULL                    COMMENT 'HTML or plain-text body — supports {{variables}}',
    is_active        CHAR(1)       NOT NULL DEFAULT 'Y'        COMMENT 'Y/N',
    created_by       BIGINT        NOT NULL,
    created_date     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by      BIGINT        NOT NULL,
    modified_date    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_email_template     UNIQUE (name),
    CONSTRAINT chk_et_type           CHECK  (template_type IN ('PAYSLIP','SALARY_ADVANCE','SALARY_INCREMENT','GENERAL')),
    CONSTRAINT chk_et_active         CHECK  (is_active     IN ('Y','N')),
    CONSTRAINT fk_et_email_config    FOREIGN KEY (email_config_id) REFERENCES email_config (id) ON DELETE SET NULL,
    CONSTRAINT fk_et_created_by      FOREIGN KEY (created_by)      REFERENCES usr          (id),
    CONSTRAINT fk_et_modified_by     FOREIGN KEY (modified_by)     REFERENCES usr          (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 13. SEED DATA
-- ============================================================

-- Bank transfer templates (V3)
INSERT IGNORE INTO bank_transfer_template
    (bank_id, bank_code, bank_name, header_template, detail_template, footer_template, file_extension, created_by, modified_by)
VALUES
    (1,  '7010', 'Bank of Ceylon',                             'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1),
    (2,  '7038', 'Standard Chartered Bank',                   NULL,                                                          '{{account_no}},{{branch_code}},{{amount}},{{employee_no}},{{name}}',   NULL,                                 'csv', 1, 1),
    (4,  '7056', 'Commercial Bank Of Ceylon PLC',             'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1),
    (6,  '7083', 'Hatton National Bank PLC',                  'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1),
    (7,  '7092', 'Hongkong and Shanghai Banking Corporation', NULL,                                                          '{{account_no}},{{branch_code}},{{amount}},{{employee_no}},{{name}}',   NULL,                                 'csv', 1, 1),
    (10, '7135', 'People''s Bank',                            'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1),
    (12, '7162', 'Nations Trust Bank PLC',                    'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1),
    (14, '7214', 'National Development Bank PLC',             'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1),
    (16, '7278', 'Sampath Bank PLC',                          'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1),
    (17, '7287', 'Seylan Bank PLC',                           'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1),
    (19, '7302', 'Union Bank Of Colombo PLC',                 'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1),
    (21, '7454', 'DFCC Bank PLC',                             'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1),
    (31, '7719', 'National Savings Bank',                     'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1),
    (33, '7737', 'HDFC Bank',                                 'H|{{bank_code}}|{{date}}|{{record_count}}|{{total_amount}}', 'D|{{account_no}}|{{branch_code}}|{{amount}}|{{employee_no}}|{{name}}', 'T|{{record_count}}|{{total_amount}}', 'txt', 1, 1);

-- Email templates (V4)
INSERT IGNORE INTO email_template (name, template_type, subject, body, is_active, created_by, modified_by) VALUES
(
  'Payslip Notification',
  'PAYSLIP',
  'Your Payslip for {{month}} {{year}} – {{company_name}}',
  '<p>Dear {{employee_name}},</p>
<p>Please find attached your payslip for <strong>{{month}} {{year}}</strong>.</p>
<table style="border-collapse:collapse;width:360px">
  <tr><td style="padding:6px 12px;background:#f8fafc;font-weight:600">Employee No</td><td style="padding:6px 12px">{{employee_no}}</td></tr>
  <tr><td style="padding:6px 12px;background:#f8fafc;font-weight:600">Department</td><td style="padding:6px 12px">{{department}}</td></tr>
  <tr><td style="padding:6px 12px;background:#f8fafc;font-weight:600">Net Pay</td><td style="padding:6px 12px"><strong>{{net_pay}}</strong></td></tr>
</table>
<p style="margin-top:20px">If you have any questions, please contact HR.</p>
<p>Regards,<br>{{company_name}} – Payroll Team</p>',
  'Y', 1, 1
),
(
  'Salary Advance Approval',
  'SALARY_ADVANCE',
  'Salary Advance Approved – {{month}} {{year}}',
  '<p>Dear {{employee_name}},</p>
<p>Your salary advance request of <strong>{{advance_amount}}</strong> for {{month}} {{year}} has been approved.</p>
<p>The amount will be reflected in your next payslip.</p>
<p>Regards,<br>{{company_name}} – Payroll Team</p>',
  'Y', 1, 1
),
(
  'Salary Increment Notice',
  'SALARY_INCREMENT',
  'Salary Increment Effective {{effective_date}} – {{company_name}}',
  '<p>Dear {{employee_name}},</p>
<p>We are pleased to inform you that your salary has been revised effective <strong>{{effective_date}}</strong>.</p>
<table style="border-collapse:collapse;width:360px">
  <tr><td style="padding:6px 12px;background:#f8fafc;font-weight:600">Previous Salary</td><td style="padding:6px 12px">{{previous_salary}}</td></tr>
  <tr><td style="padding:6px 12px;background:#f8fafc;font-weight:600">New Salary</td><td style="padding:6px 12px"><strong>{{new_salary}}</strong></td></tr>
</table>
<p>Congratulations on this achievement.</p>
<p>Regards,<br>{{company_name}} – HR Team</p>',
  'Y', 1, 1
);
