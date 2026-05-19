package com.payroll.entity;

import com.payroll.converter.BooleanToYNConverter;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fixed_allowance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedAllowance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "is_active", nullable = false, length = 1)
    private Boolean isActive;

    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "is_taxable", nullable = false, length = 1)
    private Boolean isTaxable;

    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "liable_for_epf", nullable = false, length = 1)
    private Boolean liableForEpf;

    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "liable_for_etf", nullable = false, length = 1)
    private Boolean liableForEtf;

    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "liable_for_paye", nullable = false, length = 1)
    private Boolean liableForPaye;

    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "liable_no_pay", nullable = false, length = 1)
    private Boolean liableNoPay;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_date", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdDate;

    @Column(name = "modified_by", nullable = false)
    private Long modifiedBy;

    @Column(name = "modified_date", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime modifiedDate;


}
