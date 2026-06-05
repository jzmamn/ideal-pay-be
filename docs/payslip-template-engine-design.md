# Payslip Template Engine — Enterprise System Design

> Stack: Java 21 · Spring Boot 3.x · MySQL 8 · Plain HTML/CSS · iText 7 HTML2PDF  
> Style: Multi-tenant, audit-ready, production-grade

---

## Table of Contents

1. [High-Level Architecture](#1-high-level-architecture)
2. [Processing Flow](#2-processing-flow)
3. [Database ERD & DDL](#3-database-erd--ddl)
4. [Package Structure](#4-package-structure)
5. [Entity Design](#5-entity-design)
6. [DTO Design](#6-dto-design)
7. [Repository Layer](#7-repository-layer)
8. [Service Layer](#8-service-layer)
9. [PDF Generation Service](#9-pdf-generation-service)
10. [REST API Design](#10-rest-api-design)
11. [Example Thymeleaf Template](#11-example-thymeleaf-template)
12. [Security Considerations](#12-security-considerations)
13. [Best Practices](#13-best-practices)
14. [Trade-off Analysis](#14-trade-off-analysis)

---

## 1. High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         CLIENTS / CONSUMERS                             │
│   ┌──────────────┐   ┌──────────────┐   ┌──────────────────────────┐   │
│   │  HR Admin UI │   │ Employee     │   │  Batch Scheduler         │   │
│   │  (Template   │   │  Portal      │   │  (Payroll month-end run) │   │
│   │   Management)│   │  (Download)  │   │                          │   │
│   └──────┬───────┘   └──────┬───────┘   └────────────┬─────────────┘   │
└──────────┼───────────────────┼────────────────────────┼─────────────────┘
           │  REST / HTTPS     │                        │
┌──────────▼───────────────────▼────────────────────────▼─────────────────┐
│                     Spring Boot Application Layer                        │
│                                                                          │
│  ┌─────────────────────┐  ┌──────────────────────┐  ┌────────────────┐  │
│  │ TemplateController  │  │ PayslipController     │  │ BatchJob       │  │
│  │ (CRUD + preview)    │  │ (generate/download)   │  │ (month-end)    │  │
│  └──────────┬──────────┘  └──────────┬───────────┘  └───────┬────────┘  │
│             │                        │                       │           │
│  ┌──────────▼────────────────────────▼───────────────────────▼────────┐  │
│  │                        Service Layer                                │  │
│  │  ┌─────────────────┐  ┌──────────────────┐  ┌──────────────────┐  │  │
│  │  │TemplateService  │  │PayslipDataBuilder│  │PdfGenerationSvc  │  │  │
│  │  │(CRUD, versioning│  │(builds model map │  │(Thymeleaf render │  │  │
│  │  │ validation)     │  │ from payroll run)│  │ → iText7 → Blob) │  │  │
│  │  └────────┬────────┘  └────────┬─────────┘  └────────┬─────────┘  │  │
│  └───────────┼────────────────────┼─────────────────────┼────────────┘  │
│              │                    │                      │               │
│  ┌───────────▼────────────────────▼──────────────────────▼────────────┐  │
│  │                      Repository Layer (JPA)                         │  │
│  │  CompanyRepo · TemplateRepo · TemplateVersionRepo · PayslipRepo     │  │
│  └───────────────────────────────┬─────────────────────────────────────┘  │
└──────────────────────────────────┼──────────────────────────────────────┘
                                   │
           ┌───────────────────────┼────────────────────────┐
           ▼                       ▼                        ▼
    ┌─────────────┐       ┌────────────────┐       ┌────────────────┐
    │  MySQL 8    │       │  File Storage  │       │  Audit Log     │
    │  (metadata, │       │  (S3 / local   │       │  (DB table or  │
    │   templates)│       │   PDF blobs)   │       │   log4j MDC)   │
    └─────────────┘       └────────────────┘       └────────────────┘
```

### Key Design Decisions

- **Template HTML stored in DB** — enables runtime updates without redeployment.
- **No template engine dependency** — plain HTML with `{{TOKEN}}` placeholders; a small `HtmlTemplateRenderer` replaces tokens via `String.replace()`. List sections (earnings, deductions rows) are pre-built as HTML strings and injected as single tokens.
- **iText `HtmlConverter`** — converts rendered HTML to A4 PDF; CSS handles layout.
- **Versioning with immutability** — versions are append-only; active flag on version, not template.
- **Multi-tenancy via `company_id`** — all queries are scoped to the authenticated company.

---

## 2. Processing Flow

```
Payroll Calculation Result (PayrollRun entity)
         │
         ▼
┌────────────────────────────────┐
│  PayslipDataBuilder            │
│  • Loads employee, company     │
│  • Resolves active template    │
│    version for company         │
│  • Builds Thymeleaf model map  │
│    {employee, earnings[],      │
│     deductions[], employer[],  │
│     company, period, logo}     │
└──────────────┬─────────────────┘
               │
               ▼
┌────────────────────────────────┐
│  HtmlTemplateRenderer          │
│  • Loads HTML from             │
│    PayslipTemplateVersion      │
│  • Pre-builds list fragments   │
│    (earnings/deduction rows)   │
│  • Replaces {{TOKEN}} markers  │
│  • Returns rendered HTML str   │
└──────────────┬─────────────────┘
               │
               ▼
┌────────────────────────────────┐
│  HtmlSanitizer                 │
│  • OWASP Jsoup sanitize        │
│  • Strip <script>, on* attrs   │
│  • Whitelist safe tags only    │
└──────────────┬─────────────────┘
               │
               ▼
┌────────────────────────────────┐
│  ITextPdfConverter             │
│  • ConverterProperties A4      │
│  • Font resolver (classpath)   │
│  • HtmlConverter.convertToPdf  │
│  • Returns byte[]              │
└──────────────┬─────────────────┘
               │
               ▼
┌────────────────────────────────┐
│  PdfStorageService             │
│  • Saves to S3 / local FS      │
│  • Writes employee_payslip row │
│    (storage_path, checksum,    │
│     generated_at, status)      │
└──────────────┬─────────────────┘
               │
               ▼
┌────────────────────────────────┐
│  Employee Portal               │
│  • GET /payslips/{id}/download │
│  • Streams PDF from storage    │
│  • Presigned URL (S3) or       │
│    direct stream (local)       │
└────────────────────────────────┘
```

---

## 3. Database ERD & DDL

### ERD (abbreviated)

```
company ──< payslip_template ──< payslip_template_version
   │
   └──< employee ──< payroll_run ──< employee_payslip
                                          │
                                          └── (fk) payslip_template_version
```

### MySQL DDL

```sql
-- ─────────────────────────────────────────────────────────────
-- COMPANY
-- ─────────────────────────────────────────────────────────────
CREATE TABLE company (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    code                VARCHAR(20)     NOT NULL,
    name                VARCHAR(200)    NOT NULL,
    address_line1       VARCHAR(255),
    address_line2       VARCHAR(255),
    city                VARCHAR(100),
    country             VARCHAR(100),
    phone               VARCHAR(50),
    email               VARCHAR(150),
    logo_url            VARCHAR(500),   -- S3 URL or classpath ref
    primary_color       VARCHAR(7),     -- HEX e.g. #1E3A5F
    active              TINYINT(1)      NOT NULL DEFAULT 1,
    -- audit
    created_by          VARCHAR(100)    NOT NULL,
    modified_by         VARCHAR(100),
    created_date        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_date       DATETIME        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_company_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ─────────────────────────────────────────────────────────────
-- PAYSLIP TEMPLATE  (one logical template per company)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE payslip_template (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    company_id          BIGINT          NOT NULL,
    name                VARCHAR(200)    NOT NULL,
    description         TEXT,
    active_version_id   BIGINT,         -- FK to payslip_template_version (nullable until first version)
    -- audit
    created_by          VARCHAR(100)    NOT NULL,
    modified_by         VARCHAR(100),
    created_date        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_date       DATETIME        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_pt_company FOREIGN KEY (company_id) REFERENCES company (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ─────────────────────────────────────────────────────────────
-- PAYSLIP TEMPLATE VERSION  (immutable; never update html_content)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE payslip_template_version (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    template_id         BIGINT          NOT NULL,
    version_number      INT             NOT NULL,       -- monotonically increasing per template
    html_content        LONGTEXT        NOT NULL,       -- Thymeleaf HTML
    css_content         LONGTEXT,                       -- optional separate CSS
    change_notes        VARCHAR(500),
    is_active           TINYINT(1)      NOT NULL DEFAULT 0,
    validated           TINYINT(1)      NOT NULL DEFAULT 0,  -- passed sandbox validation
    -- audit
    created_by          VARCHAR(100)    NOT NULL,
    modified_by         VARCHAR(100),
    created_date        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_date       DATETIME        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_ptv_template_version (template_id, version_number),
    CONSTRAINT fk_ptv_template FOREIGN KEY (template_id) REFERENCES payslip_template (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Back-reference from payslip_template to active version
ALTER TABLE payslip_template
    ADD CONSTRAINT fk_pt_active_version
    FOREIGN KEY (active_version_id) REFERENCES payslip_template_version (id);


-- ─────────────────────────────────────────────────────────────
-- EMPLOYEE PAYSLIP  (generated PDF record)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE employee_payslip (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    payroll_run_id          BIGINT          NOT NULL,
    employee_id             BIGINT          NOT NULL,
    company_id              BIGINT          NOT NULL,
    template_version_id     BIGINT          NOT NULL,   -- snapshot of which version generated this
    payroll_month           VARCHAR(7)      NOT NULL,   -- e.g. '2026-05'
    storage_path            VARCHAR(1000),              -- S3 key or local path
    file_size_bytes         BIGINT,
    checksum_sha256         VARCHAR(64),
    status                  ENUM('PENDING','GENERATED','FAILED','EMAILED')
                                            NOT NULL DEFAULT 'PENDING',
    generated_at            DATETIME,
    emailed_at              DATETIME,
    error_message           TEXT,
    -- audit
    created_by              VARCHAR(100)    NOT NULL,
    modified_by             VARCHAR(100),
    created_date            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_date           DATETIME        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_ep_run_employee (payroll_run_id, employee_id),
    INDEX idx_ep_company_month (company_id, payroll_month),
    INDEX idx_ep_employee (employee_id),
    CONSTRAINT fk_ep_payroll_run     FOREIGN KEY (payroll_run_id)      REFERENCES payroll_run (id),
    CONSTRAINT fk_ep_employee        FOREIGN KEY (employee_id)         REFERENCES employee (id),
    CONSTRAINT fk_ep_company         FOREIGN KEY (company_id)          REFERENCES company (id),
    CONSTRAINT fk_ep_template_ver    FOREIGN KEY (template_version_id) REFERENCES payslip_template_version (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ─────────────────────────────────────────────────────────────
-- TEMPLATE AUDIT LOG  (who changed what, when)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE payslip_template_audit (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    template_id     BIGINT          NOT NULL,
    version_id      BIGINT,
    action          VARCHAR(50)     NOT NULL,   -- CREATED, ACTIVATED, DEACTIVATED, VALIDATED
    performed_by    VARCHAR(100)    NOT NULL,
    performed_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details         TEXT,
    PRIMARY KEY (id),
    INDEX idx_pta_template (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 4. Package Structure

```
com.yourcompany.payroll
│
├── config/
│   ├── ITextConfig.java              # ConverterProperties, FontResolver bean
│   └── SecurityConfig.java
│
├── domain/                           # pure domain objects (no framework deps)
│   ├── PayslipModel.java             # value object passed to Thymeleaf
│   ├── PayComponent.java             # { label, amount } – earnings/deductions
│   └── PayPeriod.java
│
├── entity/
│   ├── Company.java
│   ├── PayslipTemplate.java
│   ├── PayslipTemplateVersion.java
│   ├── EmployeePayslip.java
│   └── audit/
│       ├── Auditable.java            # @MappedSuperclass with audit fields
│       └── PayslipTemplateAudit.java
│
├── repository/
│   ├── CompanyRepository.java
│   ├── PayslipTemplateRepository.java
│   ├── PayslipTemplateVersionRepository.java
│   └── EmployeePayslipRepository.java
│
├── dto/
│   ├── request/
│   │   ├── CreateTemplateRequest.java
│   │   ├── CreateVersionRequest.java
│   │   ├── ActivateVersionRequest.java
│   │   └── GeneratePayslipRequest.java
│   └── response/
│       ├── TemplateResponse.java
│       ├── TemplateVersionResponse.java
│       ├── PayslipResponse.java
│       └── PreviewResponse.java
│
├── service/
│   ├── PayslipTemplateService.java       # interface
│   ├── PayslipGenerationService.java     # interface
│   ├── impl/
│   │   ├── PayslipTemplateServiceImpl.java
│   │   ├── PayslipDataBuilder.java       # assembles PayslipModel from payroll run
│   │   ├── HtmlTemplateRenderer.java     # {{TOKEN}} replacement → rendered HTML
│   │   ├── HtmlSanitizerService.java     # OWASP Jsoup whitelist
│   │   ├── ITextPdfConverter.java        # HTML → byte[]
│   │   ├── PdfStorageService.java        # byte[] → S3/local + DB row
│   │   └── PayslipGenerationServiceImpl.java   # orchestrator
│   └── validation/
│       └── TemplateValidator.java        # sandbox render with dummy data
│
├── controller/
│   ├── PayslipTemplateController.java
│   └── PayslipController.java
│
├── exception/
│   ├── TemplateNotFoundException.java
│   ├── TemplateValidationException.java
│   ├── PdfGenerationException.java
│   └── GlobalExceptionHandler.java
│
└── util/
    ├── ChecksumUtil.java
    └── PayslipModelMapper.java           # MapStruct
```

---

## 5. Entity Design

```java
// ── Auditable base ────────────────────────────────────────────────────────

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    protected String createdBy;

    @LastModifiedBy
    @Column(name = "modified_by", length = 100)
    protected String modifiedBy;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    protected LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "modified_date")
    protected LocalDateTime modifiedDate;
}


// ── Company ───────────────────────────────────────────────────────────────

@Entity
@Table(name = "company")
@Getter @Setter @NoArgsConstructor
public class Company extends Auditable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String country;
    private String phone;
    private String email;

    @Column(length = 500)
    private String logoUrl;

    @Column(length = 7)
    private String primaryColor;   // #1E3A5F

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "company")
    private List<PayslipTemplate> templates = new ArrayList<>();
}


// ── PayslipTemplate ───────────────────────────────────────────────────────

@Entity
@Table(name = "payslip_template")
@Getter @Setter @NoArgsConstructor
public class PayslipTemplate extends Auditable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Nullable until first version is activated
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_version_id")
    private PayslipTemplateVersion activeVersion;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL)
    @OrderBy("versionNumber DESC")
    private List<PayslipTemplateVersion> versions = new ArrayList<>();
}


// ── PayslipTemplateVersion ────────────────────────────────────────────────

@Entity
@Table(name = "payslip_template_version")
@Getter @Setter @NoArgsConstructor
public class PayslipTemplateVersion extends Auditable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private PayslipTemplate template;

    @Column(nullable = false)
    private Integer versionNumber;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String htmlContent;         // Thymeleaf HTML — NEVER updated after creation

    @Column(columnDefinition = "LONGTEXT")
    private String cssContent;

    @Column(length = 500)
    private String changeNotes;

    @Column(nullable = false)
    private boolean active = false;

    @Column(nullable = false)
    private boolean validated = false;
}


// ── EmployeePayslip ───────────────────────────────────────────────────────

@Entity
@Table(name = "employee_payslip")
@Getter @Setter @NoArgsConstructor
public class EmployeePayslip extends Auditable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payroll_run_id", nullable = false)
    private PayrollRun payrollRun;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_version_id", nullable = false)
    private PayslipTemplateVersion templateVersion;

    @Column(nullable = false, length = 7)
    private String payrollMonth;    // "2026-05"

    @Column(length = 1000)
    private String storagePath;     // S3 key or local filesystem path

    private Long fileSizeBytes;

    @Column(length = 64)
    private String checksumSha256;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayslipStatus status = PayslipStatus.PENDING;

    private LocalDateTime generatedAt;
    private LocalDateTime emailedAt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}

public enum PayslipStatus { PENDING, GENERATED, FAILED, EMAILED }
```

---

## 6. DTO Design

```java
// ── Requests ──────────────────────────────────────────────────────────────

public record CreateTemplateRequest(
    @NotNull Long companyId,
    @NotBlank @Size(max = 200) String name,
    String description
) {}

public record CreateVersionRequest(
    @NotNull Long templateId,
    @NotBlank String htmlContent,    // raw Thymeleaf HTML
    String cssContent,
    @Size(max = 500) String changeNotes
) {}

public record ActivateVersionRequest(
    @NotNull Long versionId
) {}

public record GeneratePayslipRequest(
    @NotNull Long payrollRunId,
    @NotNull Long employeeId
) {}

// ── Responses ─────────────────────────────────────────────────────────────

public record TemplateResponse(
    Long id,
    Long companyId,
    String name,
    String description,
    Long activeVersionId,
    Integer activeVersionNumber,
    String createdBy,
    LocalDateTime createdDate
) {}

public record TemplateVersionResponse(
    Long id,
    Long templateId,
    Integer versionNumber,
    String htmlContent,
    String cssContent,
    String changeNotes,
    boolean active,
    boolean validated,
    String createdBy,
    LocalDateTime createdDate
) {}

public record PayslipResponse(
    Long id,
    Long employeeId,
    String employeeName,
    String payrollMonth,
    String status,
    LocalDateTime generatedAt,
    String downloadUrl
) {}

public record PreviewResponse(
    String renderedHtml   // sanitized; safe to display in iframe sandbox
) {}
```

---

## 7. Repository Layer

```java
// ── PayslipTemplateRepository ─────────────────────────────────────────────

public interface PayslipTemplateRepository extends JpaRepository<PayslipTemplate, Long> {

    List<PayslipTemplate> findAllByCompanyId(Long companyId);

    Optional<PayslipTemplate> findByIdAndCompanyId(Long id, Long companyId);

    // Fetch template with active version in one query
    @Query("""
        SELECT t FROM PayslipTemplate t
        LEFT JOIN FETCH t.activeVersion
        WHERE t.company.id = :companyId
        """)
    List<PayslipTemplate> findAllWithActiveVersionByCompany(@Param("companyId") Long companyId);
}


// ── PayslipTemplateVersionRepository ─────────────────────────────────────

public interface PayslipTemplateVersionRepository
        extends JpaRepository<PayslipTemplateVersion, Long> {

    List<PayslipTemplateVersion> findAllByTemplateIdOrderByVersionNumberDesc(Long templateId);

    @Query("SELECT COALESCE(MAX(v.versionNumber), 0) FROM PayslipTemplateVersion v WHERE v.template.id = :templateId")
    int findMaxVersionNumber(@Param("templateId") Long templateId);

    Optional<PayslipTemplateVersion> findByTemplateIdAndActive(Long templateId, boolean active);
}


// ── EmployeePayslipRepository ─────────────────────────────────────────────

public interface EmployeePayslipRepository extends JpaRepository<EmployeePayslip, Long> {

    Optional<EmployeePayslip> findByPayrollRunIdAndEmployeeId(Long payrollRunId, Long employeeId);

    List<EmployeePayslip> findAllByEmployeeIdOrderByPayrollMonthDesc(Long employeeId);

    @Query("""
        SELECT ep FROM EmployeePayslip ep
        WHERE ep.company.id = :companyId
          AND ep.payrollMonth = :month
        ORDER BY ep.employee.lastName, ep.employee.firstName
        """)
    List<EmployeePayslip> findByCompanyAndMonth(
            @Param("companyId") Long companyId,
            @Param("month")     String month);

    long countByCompanyIdAndPayrollMonthAndStatus(Long companyId, String month, PayslipStatus status);
}
```

---

## 8. Service Layer

```java
// ── PayslipTemplateService (interface) ────────────────────────────────────

public interface PayslipTemplateService {
    TemplateResponse        createTemplate(CreateTemplateRequest req);
    TemplateResponse        updateTemplate(Long id, CreateTemplateRequest req);
    TemplateVersionResponse createVersion(CreateVersionRequest req);
    TemplateVersionResponse activateVersion(Long templateId, ActivateVersionRequest req);
    PreviewResponse         previewVersion(Long versionId, PayslipModel sampleData);
    List<TemplateResponse>  listTemplates(Long companyId);
    TemplateVersionResponse getVersion(Long versionId);
}


// ── PayslipTemplateServiceImpl ────────────────────────────────────────────

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PayslipTemplateServiceImpl implements PayslipTemplateService {

    private final PayslipTemplateRepository         templateRepo;
    private final PayslipTemplateVersionRepository  versionRepo;
    private final PayslipTemplateAuditRepository    auditRepo;
    private final TemplateValidator                 validator;
    private final ThymeleafRenderService            renderer;
    private final HtmlSanitizerService              sanitizer;
    private final AuditorAware<String>              auditorAware;

    @Override
    public TemplateVersionResponse createVersion(CreateVersionRequest req) {
        PayslipTemplate template = templateRepo.findById(req.templateId())
                .orElseThrow(() -> new TemplateNotFoundException(req.templateId()));

        // Version numbers are monotonically increasing — never reused
        int nextVersion = versionRepo.findMaxVersionNumber(req.templateId()) + 1;

        PayslipTemplateVersion version = new PayslipTemplateVersion();
        version.setTemplate(template);
        version.setVersionNumber(nextVersion);
        version.setHtmlContent(req.htmlContent());   // stored verbatim, never edited
        version.setCssContent(req.cssContent());
        version.setChangeNotes(req.changeNotes());
        version.setActive(false);
        version.setValidated(false);

        versionRepo.save(version);
        audit(template.getId(), version.getId(), "VERSION_CREATED");
        log.info("Created template version {} for template {}", nextVersion, template.getId());

        return toVersionResponse(version);
    }

    @Override
    public TemplateVersionResponse activateVersion(Long templateId, ActivateVersionRequest req) {
        PayslipTemplate template = templateRepo.findById(templateId)
                .orElseThrow(() -> new TemplateNotFoundException(templateId));

        PayslipTemplateVersion version = versionRepo.findById(req.versionId())
                .orElseThrow(() -> new TemplateNotFoundException(req.versionId()));

        if (!version.isValidated()) {
            throw new TemplateValidationException(
                "Version %d has not been validated. Run validation before activation."
                    .formatted(version.getVersionNumber()));
        }

        // Deactivate current active version (if any)
        versionRepo.findByTemplateIdAndActive(templateId, true)
                .ifPresent(prev -> {
                    prev.setActive(false);
                    audit(templateId, prev.getId(), "DEACTIVATED");
                });

        version.setActive(true);
        template.setActiveVersion(version);

        audit(templateId, version.getId(), "ACTIVATED");
        return toVersionResponse(version);
    }

    @Override
    @Transactional(readOnly = true)
    public PreviewResponse previewVersion(Long versionId, PayslipModel sampleData) {
        PayslipTemplateVersion version = versionRepo.findById(versionId)
                .orElseThrow(() -> new TemplateNotFoundException(versionId));

        Map<String, String> tokens = modelMapper.toTokenMap(sampleData);
        String rendered  = renderer.render(version.getHtmlContent(), tokens);
        String sanitized = sanitizer.sanitize(rendered);
        return new PreviewResponse(sanitized);
    }

    private void audit(Long templateId, Long versionId, String action) {
        PayslipTemplateAudit log = new PayslipTemplateAudit();
        log.setTemplateId(templateId);
        log.setVersionId(versionId);
        log.setAction(action);
        log.setPerformedBy(auditorAware.getCurrentAuditor().orElse("system"));
        auditRepo.save(log);
    }
}


// ── PayslipGenerationService (orchestrator) ───────────────────────────────

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PayslipGenerationServiceImpl implements PayslipGenerationService {

    private final PayslipDataBuilder          dataBuilder;
    private final ThymeleafRenderService      renderer;
    private final HtmlSanitizerService        sanitizer;
    private final ITextPdfConverter           pdfConverter;
    private final PdfStorageService           storage;
    private final EmployeePayslipRepository   payslipRepo;

    @Override
    public PayslipResponse generate(GeneratePayslipRequest req) {
        log.info("Generating payslip for run={} employee={}", req.payrollRunId(), req.employeeId());

        EmployeePayslip record = payslipRepo
                .findByPayrollRunIdAndEmployeeId(req.payrollRunId(), req.employeeId())
                .orElseGet(() -> initRecord(req));

        try {
            // 1. Build model
            PayslipModel model = dataBuilder.build(req.payrollRunId(), req.employeeId());

            // 2. Resolve active template version for company
            PayslipTemplateVersion version = model.company().activeTemplate().getActiveVersion();
            if (version == null) throw new PdfGenerationException("No active template for company");

            // 3. Render HTML — replace {{TOKEN}} placeholders with real values
            Map<String, String> tokens = modelMapper.toTokenMap(model);
            String html = renderer.render(version.getHtmlContent(), tokens);

            // 4. Sanitize
            String safeHtml = sanitizer.sanitize(html);

            // 5. Convert to PDF
            byte[] pdf = pdfConverter.convert(safeHtml);

            // 6. Store & update record
            String path     = storage.store(pdf, model);
            String checksum = ChecksumUtil.sha256Hex(pdf);

            record.setStoragePath(path);
            record.setFileSizeBytes((long) pdf.length);
            record.setChecksumSha256(checksum);
            record.setTemplateVersion(version);
            record.setStatus(PayslipStatus.GENERATED);
            record.setGeneratedAt(LocalDateTime.now());
            record.setErrorMessage(null);

        } catch (Exception ex) {
            log.error("Payslip generation failed for run={}", req.payrollRunId(), ex);
            record.setStatus(PayslipStatus.FAILED);
            record.setErrorMessage(ex.getMessage());
            throw new PdfGenerationException("PDF generation failed", ex);
        } finally {
            payslipRepo.save(record);
        }

        return toResponse(record);
    }
}
```

---

## 9. PDF Generation Service

```java
// ── HtmlTemplateRenderer ──────────────────────────────────────────────────
//
// No template engine dependency. Templates are plain HTML files stored in
// MySQL. Dynamic values are inserted via {{TOKEN}} placeholder replacement.
//
// Token naming convention (all uppercase, underscores):
//   Scalars  : {{EMPLOYEE_NAME}}, {{PAYROLL_MONTH}}, {{NET_PAY}}, ...
//   Fragments: {{EARNINGS_ROWS}}, {{DEDUCTIONS_ROWS}}, {{EMPLOYER_ROWS}}
//              (pre-built <tr>...</tr> HTML blocks, injected as one string)
//
// Example template snippet:
//   <td>{{EMPLOYEE_NAME}}</td>
//   <tbody>{{EARNINGS_ROWS}}</tbody>
// ─────────────────────────────────────────────────────────────────────────

@Service
@Slf4j
public class HtmlTemplateRenderer {

    private static final String TOKEN_PREFIX = "{{";
    private static final String TOKEN_SUFFIX = "}}";

    /**
     * Renders a plain-HTML template by replacing all {{TOKEN}} markers.
     *
     * @param templateHtml raw HTML stored in payslip_template_version.html_content
     * @param model        flat map of TOKEN → replacement value
     * @return rendered HTML string ready for sanitization and PDF conversion
     */
    public String render(String templateHtml, Map<String, String> model) {
        String result = templateHtml;
        for (Map.Entry<String, String> entry : model.entrySet()) {
            String token = TOKEN_PREFIX + entry.getKey() + TOKEN_SUFFIX;
            result = result.replace(token, entry.getValue() != null ? entry.getValue() : "");
        }
        // Warn about any unreplaced tokens (catches template bugs during validation)
        if (result.contains(TOKEN_PREFIX)) {
            log.warn("Unreplaced tokens remain in rendered HTML — check template and model map");
        }
        return result;
    }
}


// ── PayslipModelMapper  (builds the flat token map) ───────────────────────

@Component
public class PayslipModelMapper {

    private static final NumberFormat NUM = NumberFormat.getInstance();
    static { NUM.setMinimumFractionDigits(2); NUM.setMaximumFractionDigits(2); }

    /**
     * Converts a {@link PayslipModel} into a flat {@code Map<String, String>}
     * where each key matches a {{TOKEN}} in the HTML template.
     */
    public Map<String, String> toTokenMap(PayslipModel m) {
        Map<String, String> map = new LinkedHashMap<>();

        // ── Company ──────────────────────────────────────────────────────
        map.put("COMPANY_NAME",         m.company().name());
        map.put("COMPANY_ADDRESS",      m.company().fullAddress());
        map.put("COMPANY_LOGO_URL",     m.company().logoUrl());
        map.put("COMPANY_PRIMARY_COLOR",m.company().primaryColor());

        // ── Employee ─────────────────────────────────────────────────────
        map.put("EMPLOYEE_NO",          m.employee().employeeNo());
        map.put("EMPLOYEE_NAME",        m.employee().firstName() + " " + m.employee().lastName());
        map.put("DESIGNATION",          m.employee().designation());
        map.put("DEPARTMENT",           m.employee().department());
        map.put("BANK_NAME",            m.employee().bankName());
        map.put("BANK_ACCOUNT",         m.employee().bankAccount());

        // ── Period ───────────────────────────────────────────────────────
        map.put("PAYROLL_MONTH",        m.period().label());        // "May 2026"
        map.put("PAY_DATE",             m.period().payDate());      // "31 May 2026"
        map.put("WORKING_DAYS",         String.valueOf(m.run().workingDays()));

        // ── Totals ───────────────────────────────────────────────────────
        map.put("GROSS_PAY",            fmt(m.run().grossPay()));
        map.put("TOTAL_DEDUCTIONS",     fmt(m.run().totalDeductions()));
        map.put("NET_PAY",              fmt(m.run().netPay()));

        // ── List fragments (pre-built HTML rows) ─────────────────────────
        map.put("EARNINGS_ROWS",        buildRows(m.earnings()));
        map.put("DEDUCTIONS_ROWS",      buildRows(m.deductions()));
        map.put("EMPLOYER_ROWS",        buildRows(m.employerContributions()));

        return map;
    }

    /**
     * Builds a sequence of HTML {@code <tr>} rows from a list of pay components.
     * The template just needs {@code {{EARNINGS_ROWS}}} inside a {@code <tbody>}.
     */
    private String buildRows(List<PayComponent> components) {
        if (components == null || components.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (PayComponent c : components) {
            sb.append("<tr>")
              .append("<td>").append(escapeHtml(c.label())).append("</td>")
              .append("<td class=\"amount\">").append(fmt(c.amount())).append("</td>")
              .append("</tr>\n");
        }
        return sb.toString();
    }

    private static String fmt(BigDecimal v) {
        if (v == null) return "0.00";
        return NUM.format(v);
    }

    /** Escapes component names to prevent stored XSS from DB data. */
    private static String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");
    }
}


// ── HtmlSanitizerService ──────────────────────────────────────────────────

@Service
public class HtmlSanitizerService {

    private static final Safelist PAYSLIP_SAFELIST = Safelist.relaxed()
            .addTags("style")                         // allow <style> for CSS
            .addAttributes(":all", "class", "style")  // allow inline style and class
            .removeTags("script")                     // explicitly strip scripts
            .removeAttributes(":all", "onerror", "onload", "onclick", "onmouseover");

    /**
     * Strips XSS vectors while preserving payslip presentation markup.
     */
    public String sanitize(String html) {
        return Jsoup.clean(html, "", PAYSLIP_SAFELIST,
                new OutputSettings().prettyPrint(false));
    }
}


// ── ITextPdfConverter ─────────────────────────────────────────────────────

@Service
@Slf4j
public class ITextPdfConverter {

    private final ConverterProperties converterProperties;

    public ITextPdfConverter() {
        // A4 portrait, classpath font resolver
        FontProvider fontProvider = new DefaultFontProvider(true, true, false);

        this.converterProperties = new ConverterProperties()
                .setFontProvider(fontProvider)
                .setBaseUri("classpath:/");   // resolves relative CSS/image paths
    }

    /**
     * Converts a fully-rendered, sanitized HTML string to a PDF byte array.
     * The page size is set via CSS in the template:
     *   @page { size: A4 portrait; margin: 20mm; }
     */
    public byte[] convert(String html) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            HtmlConverter.convertToPdf(html, baos, converterProperties);
            return baos.toByteArray();
        } catch (Exception ex) {
            log.error("iText HTML→PDF conversion failed", ex);
            throw new PdfGenerationException("HTML to PDF conversion failed", ex);
        }
    }
}


// ── PdfStorageService ─────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfStorageService {

    @Value("${payslip.storage.type:local}")   // "local" | "s3"
    private String storageType;

    @Value("${payslip.storage.local.base-path:/var/payslips}")
    private String localBasePath;

    // Inject S3Client if storageType == "s3"

    public String store(byte[] pdf, PayslipModel model) {
        String filename = buildFilename(model);
        if ("s3".equals(storageType)) {
            return storeS3(pdf, filename);
        }
        return storeLocal(pdf, filename);
    }

    private String storeLocal(byte[] pdf, String filename) {
        Path target = Path.of(localBasePath, filename);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, pdf, StandardOpenOption.CREATE_NEW);
            return target.toString();
        } catch (IOException ex) {
            throw new PdfGenerationException("Failed to write PDF to disk", ex);
        }
    }

    private String buildFilename(PayslipModel model) {
        // e.g. 2026/05/COMP001/EMP001_2026-05.pdf
        return "%s/%s/%s/%s_%s.pdf".formatted(
            model.period().year(),
            model.period().monthPadded(),
            model.company().code(),
            model.employee().employeeNo(),
            model.period().yearMonth()
        );
    }
}


// ── TemplateValidator ─────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateValidator {

    private final ThymeleafRenderService renderer;
    private final ITextPdfConverter      pdfConverter;

    private static final PayslipModel DUMMY = PayslipModel.dummyForValidation();

    /**
     * Validates a template by rendering it with dummy data and converting to PDF.
     * If either step throws, the template is rejected.
     *
     * @throws TemplateValidationException if rendering or PDF conversion fails
     */
    public void validate(PayslipTemplateVersion version) {
        try {
            Map<String, String> tokens = modelMapper.toTokenMap(DUMMY);
            String html = renderer.render(version.getHtmlContent(), tokens);
            pdfConverter.convert(html);
            version.setValidated(true);
            log.info("Template version {} passed validation", version.getId());
        } catch (Exception ex) {
            log.warn("Template version {} failed validation: {}", version.getId(), ex.getMessage());
            throw new TemplateValidationException(
                "Template validation failed: " + ex.getMessage(), ex);
        }
    }
}
```

---

## 10. REST API Design

### Endpoints

| Method   | Path                                                    | Description                          |
|----------|---------------------------------------------------------|--------------------------------------|
| `POST`   | `/api/templates`                                        | Create a new template                |
| `PUT`    | `/api/templates/{id}`                                   | Update template metadata             |
| `GET`    | `/api/templates?companyId={id}`                         | List templates for a company         |
| `POST`   | `/api/templates/{id}/versions`                          | Create a new version (append-only)   |
| `GET`    | `/api/templates/{id}/versions`                          | List all versions of a template      |
| `POST`   | `/api/templates/{id}/versions/{vId}/validate`           | Validate before activation           |
| `POST`   | `/api/templates/{id}/activate`                          | Activate a specific version          |
| `GET`    | `/api/templates/{id}/versions/{vId}/preview`            | Preview version (HTML response)      |
| `POST`   | `/api/payslips/generate`                                | Generate one payslip                 |
| `POST`   | `/api/payslips/generate-batch`                          | Generate all payslips for a month    |
| `GET`    | `/api/payslips/{id}/download`                           | Stream PDF to client                 |
| `GET`    | `/api/payslips?employeeId={id}`                         | List payslips for an employee        |

### Controller Skeletons

```java
// ── PayslipTemplateController ─────────────────────────────────────────────

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@Validated
public class PayslipTemplateController {

    private final PayslipTemplateService templateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TemplateResponse create(@Valid @RequestBody CreateTemplateRequest req) {
        return templateService.createTemplate(req);
    }

    @PutMapping("/{id}")
    public TemplateResponse update(@PathVariable Long id,
                                   @Valid @RequestBody CreateTemplateRequest req) {
        return templateService.updateTemplate(id, req);
    }

    @GetMapping
    public List<TemplateResponse> list(@RequestParam Long companyId) {
        return templateService.listTemplates(companyId);
    }

    @PostMapping("/{id}/versions")
    @ResponseStatus(HttpStatus.CREATED)
    public TemplateVersionResponse createVersion(@PathVariable Long id,
                                                  @Valid @RequestBody CreateVersionRequest req) {
        return templateService.createVersion(req.withTemplateId(id));
    }

    @PostMapping("/{id}/versions/{vId}/validate")
    public TemplateVersionResponse validate(@PathVariable Long id, @PathVariable Long vId) {
        return templateService.validateVersion(vId);
    }

    @PostMapping("/{id}/activate")
    public TemplateVersionResponse activate(@PathVariable Long id,
                                             @Valid @RequestBody ActivateVersionRequest req) {
        return templateService.activateVersion(id, req);
    }

    @GetMapping("/{id}/versions/{vId}/preview")
    public ResponseEntity<PreviewResponse> preview(@PathVariable Long id,
                                                    @PathVariable Long vId) {
        PreviewResponse preview = templateService.previewVersion(vId, PayslipModel.dummyForValidation());
        return ResponseEntity.ok()
                .header("Content-Security-Policy", "sandbox allow-same-origin")
                .body(preview);
    }
}


// ── PayslipController ─────────────────────────────────────────────────────

@RestController
@RequestMapping("/api/payslips")
@RequiredArgsConstructor
public class PayslipController {

    private final PayslipGenerationService generationService;
    private final EmployeePayslipRepository payslipRepo;
    private final PdfStorageService storage;

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PayslipResponse generate(@Valid @RequestBody GeneratePayslipRequest req) {
        return generationService.generate(req);
    }

    @PostMapping("/generate-batch")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Object> generateBatch(@RequestParam Long companyId,
                                              @RequestParam String payrollMonth) {
        return generationService.generateBatch(companyId, payrollMonth);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable Long id) {
        EmployeePayslip ep = payslipRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payslip", id));

        StreamingResponseBody body = outputStream ->
                storage.stream(ep.getStoragePath(), outputStream);

        String filename = "payslip-%s-%s.pdf".formatted(
                ep.getEmployee().getEmployeeNo(), ep.getPayrollMonth());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(body);
    }

    @GetMapping
    public List<PayslipResponse> list(@RequestParam Long employeeId) {
        return payslipRepo.findAllByEmployeeIdOrderByPayrollMonthDesc(employeeId)
                .stream().map(this::toResponse).toList();
    }
}
```

---

## 11. Example Plain HTML Template

The template stored in `payslip_template_version.html_content` is **pure HTML and CSS**.  
No template-engine attributes, no server-side syntax — just `{{TOKEN}}` markers where data goes.

HR admins can open this file in any browser and see a fully readable payslip skeleton.  
`{{EARNINGS_ROWS}}`, `{{DEDUCTIONS_ROWS}}`, and `{{EMPLOYER_ROWS}}` are replaced with  
pre-built `<tr>…</tr>` blocks by `PayslipModelMapper.buildRows()` before injection.

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <style>
    /* ── Page setup ── */
    @page {
      size: A4 portrait;
      margin: 18mm 15mm 18mm 15mm;
    }
    * { box-sizing: border-box; font-family: 'Helvetica Neue', Arial, sans-serif; }
    body { margin: 0; font-size: 10pt; color: #222; }

    /* ── Header band ──
       {{COMPANY_PRIMARY_COLOR}} is replaced with the company's hex color, e.g. #1E3A5F
       This is the ONE place a token appears inside CSS — keep it on its own line. */
    .header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      border-bottom: 3px solid {{COMPANY_PRIMARY_COLOR}};
      padding-bottom: 8pt;
      margin-bottom: 12pt;
    }
    .company-name { font-size: 16pt; font-weight: 700; color: {{COMPANY_PRIMARY_COLOR}}; }
    .company-addr { font-size: 8pt;  color: #555; margin-top: 3pt; }
    .doc-title    { text-align: right; }
    .doc-title h2 { font-size: 13pt; margin: 0; color: #333; }
    .period       { font-size: 10pt; color: #666; }

    /* ── Employee section ── */
    .emp-section {
      display: flex;
      gap: 20pt;
      background: #f8f9fb;
      border: 1px solid #e2e8f0;
      border-radius: 4pt;
      padding: 8pt 12pt;
      margin-bottom: 14pt;
      font-size: 9pt;
    }
    .emp-col   { flex: 1; }
    .emp-label { color: #64748b; width: 90pt; display: inline-block; }
    .emp-value { font-weight: 600; }

    /* ── Components tables ── */
    table { width: 100%; border-collapse: collapse; margin-bottom: 12pt; }
    thead th {
      background: {{COMPANY_PRIMARY_COLOR}};
      color: #fff; font-size: 9pt; font-weight: 600;
      padding: 5pt 8pt; text-align: left;
    }
    thead th.amount { text-align: right; }
    tbody td { padding: 4pt 8pt; font-size: 9pt; border-bottom: 1px solid #f0f0f0; }
    td.amount { text-align: right; font-variant-numeric: tabular-nums; }
    .total-row td { font-weight: 700; border-top: 1.5px solid #cbd5e1; border-bottom: none; }

    /* ── Net pay band ── */
    .net-pay {
      background: {{COMPANY_PRIMARY_COLOR}};
      color: #fff;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8pt 12pt;
      border-radius: 4pt;
      margin-bottom: 12pt;
    }
    .net-pay .label  { font-size: 12pt; font-weight: 600; }
    .net-pay .amount { font-size: 15pt; font-weight: 700; }

    /* ── Footer ── */
    .footer {
      font-size: 7.5pt; color: #94a3b8; text-align: center;
      margin-top: 16pt; border-top: 1px solid #e2e8f0; padding-top: 6pt;
    }
  </style>
</head>
<body>

  <!-- ── Header ── -->
  <div class="header">
    <div>
      <img src="{{COMPANY_LOGO_URL}}" alt="Company Logo"
           style="max-height:40pt; margin-bottom:4pt;"/>
      <div class="company-name">{{COMPANY_NAME}}</div>
      <div class="company-addr">{{COMPANY_ADDRESS}}</div>
    </div>
    <div class="doc-title">
      <h2>PAY SLIP</h2>
      <div class="period">{{PAYROLL_MONTH}}</div>
    </div>
  </div>

  <!-- ── Employee info ── -->
  <div class="emp-section">
    <div class="emp-col">
      <div><span class="emp-label">Employee No.</span><span class="emp-value">{{EMPLOYEE_NO}}</span></div>
      <div><span class="emp-label">Name</span>        <span class="emp-value">{{EMPLOYEE_NAME}}</span></div>
      <div><span class="emp-label">Designation</span> <span class="emp-value">{{DESIGNATION}}</span></div>
    </div>
    <div class="emp-col">
      <div><span class="emp-label">Department</span>  <span class="emp-value">{{DEPARTMENT}}</span></div>
      <div><span class="emp-label">Bank</span>        <span class="emp-value">{{BANK_NAME}}</span></div>
      <div><span class="emp-label">Account No.</span> <span class="emp-value">{{BANK_ACCOUNT}}</span></div>
    </div>
    <div class="emp-col">
      <div><span class="emp-label">Working Days</span><span class="emp-value">{{WORKING_DAYS}}</span></div>
      <div><span class="emp-label">Pay Date</span>   <span class="emp-value">{{PAY_DATE}}</span></div>
    </div>
  </div>

  <!-- ── Earnings ──
       {{EARNINGS_ROWS}} expands to one <tr> per earning component, built by PayslipModelMapper -->
  <table>
    <thead>
      <tr><th>Earnings</th><th class="amount">Amount (LKR)</th></tr>
    </thead>
    <tbody>
      {{EARNINGS_ROWS}}
    </tbody>
    <tfoot>
      <tr class="total-row">
        <td>Gross Pay</td>
        <td class="amount">{{GROSS_PAY}}</td>
      </tr>
    </tfoot>
  </table>

  <!-- ── Deductions ── -->
  <table>
    <thead>
      <tr><th>Deductions</th><th class="amount">Amount (LKR)</th></tr>
    </thead>
    <tbody>
      {{DEDUCTIONS_ROWS}}
    </tbody>
    <tfoot>
      <tr class="total-row">
        <td>Total Deductions</td>
        <td class="amount">{{TOTAL_DEDUCTIONS}}</td>
      </tr>
    </tfoot>
  </table>

  <!-- ── Net Pay ── -->
  <div class="net-pay">
    <span class="label">NET PAY</span>
    <span class="amount">LKR {{NET_PAY}}</span>
  </div>

  <!-- ── Employer Contributions ──
       {{EMPLOYER_ROWS}} is empty string when there are no employer contributions,
       so the table still renders but with an empty tbody — hide it with CSS if needed. -->
  <table>
    <thead>
      <tr><th>Employer Contributions</th><th class="amount">Amount (LKR)</th></tr>
    </thead>
    <tbody>
      {{EMPLOYER_ROWS}}
    </tbody>
  </table>

  <!-- ── Footer ── -->
  <div class="footer">
    This payslip is system-generated and does not require a signature. · Confidential.
  </div>

</body>
</html>
```

### Token Reference

| Token | Example value | Source |
|---|---|---|
| `{{COMPANY_NAME}}` | `Ideal Technologies (Pvt) Ltd` | `company.name` |
| `{{COMPANY_ADDRESS}}` | `123 Main St, Colombo, Sri Lanka` | `company.fullAddress()` |
| `{{COMPANY_LOGO_URL}}` | `https://cdn.example.com/logo.png` | `company.logoUrl` |
| `{{COMPANY_PRIMARY_COLOR}}` | `#1E3A5F` | `company.primaryColor` |
| `{{EMPLOYEE_NO}}` | `EMP001` | `employee.employeeNo` |
| `{{EMPLOYEE_NAME}}` | `John Doe` | `firstName + " " + lastName` |
| `{{DESIGNATION}}` | `Software Engineer` | `employee.designation` |
| `{{DEPARTMENT}}` | `Engineering` | `employee.department` |
| `{{BANK_NAME}}` | `Bank of Ceylon` | `employee.bankName` |
| `{{BANK_ACCOUNT}}` | `1234567890` | `employee.bankAccount` |
| `{{PAYROLL_MONTH}}` | `May 2026` | `period.label()` |
| `{{PAY_DATE}}` | `31 May 2026` | `period.payDate()` |
| `{{WORKING_DAYS}}` | `22` | `run.workingDays` |
| `{{GROSS_PAY}}` | `127,500.00` | `run.grossPay` |
| `{{TOTAL_DEDUCTIONS}}` | `15,500.00` | `run.totalDeductions` |
| `{{NET_PAY}}` | `112,000.00` | `run.netPay` |
| `{{EARNINGS_ROWS}}` | `<tr>…</tr><tr>…</tr>` | built by `buildRows(earnings)` |
| `{{DEDUCTIONS_ROWS}}` | `<tr>…</tr>` | built by `buildRows(deductions)` |
| `{{EMPLOYER_ROWS}}` | `<tr>…</tr>` | built by `buildRows(employerContributions)` |

---

## 12. Security Considerations

### HTML Injection Prevention

```java
// Never pass raw user input into Thymeleaf variables.
// Thymeleaf's th:text performs HTML-entity escaping automatically.
// Only th:utext skips escaping — do NOT use it in payslip templates.

// Template content stored in DB comes from HR admins (trusted),
// but must still pass the validator before activation.
// Sanitize ALL rendered output before PDF conversion using Jsoup.
```

### Template Validation Gate

```
HR Admin uploads HTML
        │
        ▼
POST /api/templates/{id}/versions          → stored, validated=false
        │
        ▼
POST /api/templates/{id}/versions/{vId}/validate
  • Render with dummy PayslipModel
  • Convert to PDF
  • If either throws → 422 Unprocessable Entity
  • If both succeed → validated=true
        │
        ▼
POST /api/templates/{id}/activate          → only if validated=true
```

### Audit Tracking

Every state transition is written to `payslip_template_audit`:

| Action            | Trigger                              |
|-------------------|--------------------------------------|
| `VERSION_CREATED` | `createVersion()`                    |
| `VALIDATED`       | `validateVersion()`                  |
| `ACTIVATED`       | `activateVersion()`                  |
| `DEACTIVATED`     | Previous version when new activates  |
| `PAYSLIP_GENERATED` | `generate()` success               |
| `PAYSLIP_FAILED`  | `generate()` failure                 |

### `@MappedSuperclass` Audit Fields

```java
// Enable Spring Data auditing in your @SpringBootApplication class:
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")

@Bean
AuditorAware<String> auditorProvider() {
    return () -> Optional.ofNullable(SecurityContextHolder.getContext())
            .map(SecurityContext::getAuthentication)
            .filter(Authentication::isAuthenticated)
            .map(Authentication::getName);
}
```

---

## 13. Best Practices

### Immutable Versions

```
RULE: payslip_template_version.html_content is WRITE-ONCE.
Never issue UPDATE on this column after initial INSERT.
All changes create a new version row with version_number = max + 1.
This gives a complete audit trail of every template that ever generated a payslip.
```

### Dynamic Components — Model Population

```java
// PayslipDataBuilder — never hardcode component names
public PayslipModel build(Long runId, Long employeeId) {
    EmpPayrollRun run = runRepo.findById(runId).orElseThrow(...);

    List<PayComponent> earnings = new ArrayList<>();
    earnings.add(new PayComponent("Basic Salary", run.getBasicSalary()));

    for (EmpPayrollRunDetail d : run.getDetails()) {
        if (isEarning(d.getComponentType()))
            earnings.add(new PayComponent(d.getComponentName(), d.getAmount()));
    }

    List<PayComponent> deductions = run.getDetails().stream()
            .filter(d -> isDeduction(d.getComponentType()))
            .map(d -> new PayComponent(d.getComponentName(), d.getAmount()))
            .toList();

    // ... build and return PayslipModel record
}
```

### PDF Caching

For high-volume month-end runs, cache the rendered PDF:

```java
@Cacheable(value = "payslip-pdf", key = "#payslipId", unless = "#result == null")
public byte[] getCachedPdf(Long payslipId) { ... }

@CacheEvict(value = "payslip-pdf", key = "#payslipId")
public void regenerate(Long payslipId) { ... }
```

### Async Batch Generation

```java
@Async("payslipTaskExecutor")
public CompletableFuture<PayslipResponse> generateAsync(GeneratePayslipRequest req) {
    return CompletableFuture.completedFuture(generate(req));
}

// Thread pool configuration
@Bean("payslipTaskExecutor")
public TaskExecutor payslipExecutor() {
    ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
    exec.setCorePoolSize(4);
    exec.setMaxPoolSize(10);
    exec.setQueueCapacity(500);
    exec.setThreadNamePrefix("payslip-");
    exec.initialize();
    return exec;
}
```

### Multi-Tenant Isolation

```java
// All queries must be scoped by company_id resolved from the JWT
// Never accept companyId from the request body for tenant-sensitive operations
Long companyId = securityContext.getCompanyId();  // from JWT claim
```

---

## 14. Trade-off Analysis

| Decision | Alternative | Trade-off |
|---|---|---|
| Templates stored in DB | Files on filesystem | DB allows runtime updates without deploy; filesystem is simpler for large assets |
| Plain HTML `{{TOKEN}}` replacement | Thymeleaf / Mustache / Freemarker | Zero dependency, templates open in any browser, easy for HR admins; no loop syntax (pre-build row HTML in Java instead) |
| iText 7 HTML2PDF | Flying Saucer / Apache FOP | iText is actively maintained, better CSS support; licensed (AGPL or commercial) |
| Append-only versions | In-place update | Immutability enables forensic audit; costs more DB rows over time |
| Jsoup sanitize + validated flag | Trust HR admins | Defense-in-depth; prevents stored XSS if admin account is compromised |
| Async batch generation | Synchronous | Better UX for large payrolls; adds queue/retry complexity |
| Local FS storage | S3 | S3 is production-ready at scale; local is simpler for single-node dev |

### What to Revisit at Scale

1. **Template caching** — move from in-process to Redis when multiple app nodes run.
2. **PDF storage** — switch to S3 with presigned URLs once storage > 1 GB.
3. **Batch generation** — introduce a job queue (RabbitMQ / SQS) for payrolls > 1 000 employees.
4. **Multi-tenancy** — consider row-level security or schema-per-tenant when client count grows.
5. **Font embedding** — add custom font resolver to iText if branding requires non-standard fonts.
