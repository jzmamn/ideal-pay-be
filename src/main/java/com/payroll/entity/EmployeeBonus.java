package com.payroll.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.payroll.converter.BooleanToYNConverter;
import com.payroll.enums.BonusStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "emp_bonus")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeBonus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT 0.00")
    private BigDecimal amount;

    /** Amount overridden by an approver. When not null, this is the effective payment amount. */
    @Column(name = "adjusted_amount", precision = 15, scale = 2)
    private BigDecimal adjustedAmount;

    /** MVEL expression used to compute the amount during bonus processing. */
    @Column(name = "formula_expression", length = 500)
    private String formulaExpression;

    /** Raw value returned by the formula engine before rounding. */
    @Column(name = "formula_result", precision = 15, scale = 2)
    private BigDecimal formulaResult;

    @Column(name = "payroll_month", nullable = false, length = 20)
    private String payrollMonth;

    /** Workflow status for managed bonus-processing runs. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private BonusStatus status = BonusStatus.PENDING;

    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "is_processed", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private Boolean isProcessed;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    /** Approver reference — set when status transitions to APPROVED. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    @JsonIgnoreProperties({"role", "createdBy", "modifiedBy", "hibernateLazyInitializer", "handler"})
    private Usr approvedBy;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    /** Audit note — used when an adjustment is applied. */
    @Column(name = "note", length = 500)
    private String note;

    /** Links this entry to its bonus processing batch. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processing_batch_id")
    @JsonIgnoreProperties({"createdBy", "modifiedBy", "approvedBy", "processedBy", "hibernateLazyInitializer", "handler"})
    private BonusProcessingBatch processingBatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    @JsonIgnoreProperties({"createdBy", "modifiedBy", "hibernateLazyInitializer", "handler"})
    private Employee employee;

    /** Optional link to the master Bonus definition. Nullable for backward compatibility. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bonus_id", nullable = true)
    @JsonIgnoreProperties({"createdBy", "modifiedBy", "hibernateLazyInitializer", "handler"})
    private Bonus bonus;

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

    /** Set when the row was created via file import; enables import rollback. */
    @Column(name = "import_log_id")
    private Long importLogId;
}
