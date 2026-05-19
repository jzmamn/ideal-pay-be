package com.payroll.entity;

import com.payroll.converter.BooleanToYNConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "variable_deduction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariableDeduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "is_active", nullable = false, length = 1)
    private Boolean isActive;

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
