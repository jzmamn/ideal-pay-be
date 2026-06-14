package com.payroll.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.payroll.converter.BooleanToYNConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "late_deduction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LateDeductionConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = true, unique = true, length = 10)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", nullable = true, length = 255)
    private String description;

    /** Number of working days per month used in the default formula. Default: 26. */
    @Builder.Default
    @Column(name = "working_days", nullable = false)
    private Integer workingDays = 26;

    /** Working hours per day used in the default formula. Default: 8. */
    @Builder.Default
    @Column(name = "working_hours_per_day", nullable = false)
    private Integer workingHoursPerDay = 8;

    /**
     * Optional MVEL formula expression.
     * Available context variables: basicSalary, workingDays, workingHoursPerDay, lateHours.
     * Default behaviour (formulaEnabled = false):
     *   basicSalary / (workingDays * workingHoursPerDay) * lateHours
     */
    @Column(name = "formula", nullable = true, length = 500)
    private String formula;

    @Builder.Default
    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "formula_enabled", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private Boolean formulaEnabled = false;

    @Builder.Default
    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "is_active", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private Boolean isActive = true;

    // ── Statutory liability flags ────────────────────────────────────────────

    @Builder.Default
    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "liable_for_epf", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private Boolean liableForEpf = true;

    @Builder.Default
    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "liable_for_etf", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private Boolean liableForEtf = true;

    @Builder.Default
    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "liable_for_paye", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private Boolean liableForPaye = true;

    @Builder.Default
    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "liable_for_nopay", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private Boolean liableForNopay = false;

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
}
