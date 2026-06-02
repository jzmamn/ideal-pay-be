package com.payroll.entity;

import com.payroll.converter.BooleanToYNConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "salary_increment_detail",
       uniqueConstraints = @UniqueConstraint(name = "uk_si_detail", columnNames = {"increment_id","emp_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SalaryIncrementDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "increment_id", nullable = false)
    private SalaryIncrement increment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"createdBy","modifiedBy","hibernateLazyInitializer","handler"})
    private Employee employee;

    @Column(name = "current_basic", nullable = false, precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0.00")
    private BigDecimal currentBasic = BigDecimal.ZERO;

    @Column(name = "increment_basic", nullable = false, precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0.00")
    private BigDecimal incrementBasic = BigDecimal.ZERO;

    @Column(name = "new_basic", nullable = false, precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0.00")
    private BigDecimal newBasic = BigDecimal.ZERO;

    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "is_exported", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private Boolean isExported = false;

    @Column(name = "exported_date")
    private LocalDateTime exportedDate;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @OneToMany(mappedBy = "detail", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalaryIncrementFa> faIncrements = new ArrayList<>();

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
