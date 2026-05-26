package com.payroll.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.payroll.converter.BooleanToYNConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "nopay_days")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NopayDays {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = true, unique = true, length = 10)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "days", nullable = false, precision = 5, scale = 2)
    private BigDecimal days;

    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "is_active", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private Boolean isActive;

    /**
     * Optional MVEL formula that computes the nopay deduction at payroll run time.
     * Available variables: basicSalary, workingDays, nopayDays, + custom vars.
     */
    @Column(name = "formula", nullable = true, length = 500)
    private String formula;

    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "formula_enabled", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private Boolean formulaEnabled = false;

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
