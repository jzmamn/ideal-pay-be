package com.payroll.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "salary_increment_fa",
       uniqueConstraints = @UniqueConstraint(name = "uk_si_fa", columnNames = {"detail_id","fa_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SalaryIncrementFa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detail_id", nullable = false)
    private SalaryIncrementDetail detail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fa_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"createdBy","modifiedBy","hibernateLazyInitializer","handler"})
    private FixedAllowance fixedAllowance;

    @Column(name = "current_amount", nullable = false, precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0.00")
    private BigDecimal currentAmount = BigDecimal.ZERO;

    @Column(name = "increment_amount", nullable = false, precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0.00")
    private BigDecimal incrementAmount = BigDecimal.ZERO;

    @Column(name = "new_amount", nullable = false, precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0.00")
    private BigDecimal newAmount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"role","createdBy","modifiedBy","hibernateLazyInitializer","handler"})
    private Usr createdBy;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"role","createdBy","modifiedBy","hibernateLazyInitializer","handler"})
    private Usr modifiedBy;

    @UpdateTimestamp
    @Column(name = "modified_date", nullable = false)
    private LocalDateTime modifiedDate;
}
