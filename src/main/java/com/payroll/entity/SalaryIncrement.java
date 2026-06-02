package com.payroll.entity;

import com.payroll.enums.IncrementStatus;
import com.payroll.enums.IncrementType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "salary_increment")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SalaryIncrement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private IncrementType type;

    @Column(name = "effective_month", nullable = false, length = 20)
    private String effectiveMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private IncrementStatus status = IncrementStatus.DRAFT;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @OneToMany(mappedBy = "increment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalaryIncrementDetail> details = new ArrayList<>();

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
