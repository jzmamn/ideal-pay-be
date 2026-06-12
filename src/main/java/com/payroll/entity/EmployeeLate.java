package com.payroll.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.payroll.converter.BooleanToYNConverter;
import jakarta.persistence.*;
import com.payroll.entity.LateDeductionConfig;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "emp_late")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeLate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Rate per hour computed during the load phase (config formula or basicSalary/(workingDays*8)). */
    @Column(name = "rate", nullable = false, precision = 15, scale = 6,
            columnDefinition = "DECIMAL(15,6) DEFAULT 0.000000")
    private BigDecimal rate = BigDecimal.ZERO;

    @Column(name = "hours", nullable = false, precision = 5, scale = 2,
            columnDefinition = "DECIMAL(5,2) DEFAULT 0.00")
    private BigDecimal hours;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT 0.00")
    private BigDecimal amount;

    @Column(name = "payroll_month", length = 20)
    private String payrollMonth;

    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "is_processed", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private Boolean isProcessed;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    @JsonIgnoreProperties({"createdBy", "modifiedBy", "hibernateLazyInitializer", "handler"})
    private Employee employee;

    /** Optional link to the LateDeductionConfig that generated this rate. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "late_config_id", nullable = true)
    @JsonIgnoreProperties({"createdBy", "modifiedBy", "hibernateLazyInitializer", "handler"})
    private LateDeductionConfig lateConfig;

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
