package com.payroll.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "emp_transfer_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmpTransferLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_run_id", nullable = false)
    @JsonIgnoreProperties({"details", "employee", "createdBy", "modifiedBy", "processedBy", "hibernateLazyInitializer", "handler"})
    private EmpPayrollRun payrollRun;

    @Column(name = "transfer_type", nullable = false, length = 20)
    private String transferType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id")
    @JsonIgnoreProperties({"createdBy", "modifiedBy", "hibernateLazyInitializer", "handler"})
    private Bank bank;

    @Column(name = "bank_code", length = 20)
    private String bankCode;

    @Column(name = "transferred_amount", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT '0.00'")
    @Builder.Default
    private BigDecimal transferredAmount = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "transferred_date", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime transferredDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transferred_by", nullable = false)
    @JsonIgnoreProperties({"role", "createdBy", "modifiedBy", "hibernateLazyInitializer", "handler"})
    private Usr transferredBy;
}
