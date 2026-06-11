package com.payroll.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.payroll.converter.BooleanToYNConverter;
import com.payroll.enums.PayrollStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A company payroll period (one per company / year / month).
 * Lifecycle: FUTURE → OPEN → PROCESSING → COMPLETED → CLOSED ⇄ REOPENED.
 */
@Entity
@Table(
    name = "payroll_period",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_company_period",
        columnNames = {"company_id", "period_year", "period_month"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    @JsonIgnoreProperties({"createdBy", "modifiedBy", "hibernateLazyInitializer", "handler"})
    private Company company;

    @Column(name = "period_year", nullable = false)
    private Integer periodYear;

    /** 1–12 */
    @Column(name = "period_month", nullable = false)
    private Integer periodMonth;

    /** Auto-generated, format: YYYY-MM e.g. 2026-06 */
    @Column(name = "period_code", nullable = false, length = 10)
    private String periodCode;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /** Number of working/payable days (used for nopay and OT rate calculation). */
    @Builder.Default
    @Column(name = "working_days", nullable = false, columnDefinition = "INT DEFAULT 26")
    private Integer workingDays = 26;

    @Enumerated(EnumType.STRING)
    @Column(name = "payroll_status", nullable = false, length = 20,
            columnDefinition = "VARCHAR(20) DEFAULT 'FUTURE'")
    @Builder.Default
    private PayrollStatus payrollStatus = PayrollStatus.FUTURE;

    /** 'Y' when payroll inputs are locked (PROCESSING, COMPLETED, CLOSED). */
    @Builder.Default
    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "locked", nullable = false, length = 1,
            columnDefinition = "CHAR(1) DEFAULT 'N'")
    private Boolean locked = false;

    /** Only one active period is allowed per company. */
    @Builder.Default
    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "active", nullable = false, length = 1,
            columnDefinition = "CHAR(1) DEFAULT 'N'")
    private Boolean active = false;

    @Column(name = "payroll_run_date")
    private LocalDate payrollRunDate;

    @Column(name = "closed_date")
    private LocalDateTime closedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closed_by")
    @JsonIgnoreProperties({"role", "createdBy", "modifiedBy", "hibernateLazyInitializer", "handler"})
    private Usr closedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnoreProperties({"role", "createdBy", "modifiedBy", "hibernateLazyInitializer", "handler"})
    private Usr createdBy;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by", nullable = false)
    @JsonIgnoreProperties({"role", "createdBy", "modifiedBy", "hibernateLazyInitializer", "handler"})
    private Usr modifiedBy;

    @UpdateTimestamp
    @Column(name = "modified_date", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime modifiedDate;

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Builds the YYYY-MM code from year + month. */
    public static String buildPeriodCode(int year, int month) {
        return String.format("%04d-%02d", year, month);
    }

    @PrePersist
    @PreUpdate
    private void ensurePeriodCode() {
        if (periodYear != null && periodMonth != null) {
            this.periodCode = buildPeriodCode(periodYear, periodMonth);
        }
    }
}
