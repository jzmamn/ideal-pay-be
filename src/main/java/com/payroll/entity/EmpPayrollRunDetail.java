package com.payroll.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.payroll.enums.ComponentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "emp_payroll_run_detail",
    uniqueConstraints = @UniqueConstraint(name = "uk_run_detail", columnNames = {"run_id", "component_type", "component_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpPayrollRunDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    @JsonIgnoreProperties({"details", "employee", "createdBy", "modifiedBy", "hibernateLazyInitializer", "handler"})
    private EmpPayrollRun payrollRun;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false, length = 10)
    private ComponentType componentType;

    @Column(name = "component_id", nullable = false)
    private Long componentId;

    @Column(name = "component_code", nullable = false, length = 20)
    private String componentCode;

    @Column(name = "component_name", nullable = false, length = 150)
    private String componentName;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT '0.00'")
    private BigDecimal amount;

    /** Applicable for OT only */
    @Column(name = "hours", precision = 5, scale = 2)
    private BigDecimal hours;

    /** Applicable for NOPAY only */
    @Column(name = "days", precision = 5, scale = 2)
    private BigDecimal days;

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
