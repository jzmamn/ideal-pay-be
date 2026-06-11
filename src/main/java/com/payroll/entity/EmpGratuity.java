package com.payroll.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.payroll.enums.GratuityStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "emp_gratuity")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmpGratuity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    @JsonIgnoreProperties({"createdBy", "modifiedBy", "hibernateLazyInitializer", "handler"})
    private Employee employee;

    /** Date the employee's service ends (resignation / retirement / termination). */
    @Column(name = "termination_date", nullable = false)
    private LocalDate terminationDate;

    /** Joined date snapshotted at entry time. */
    @Column(name = "joined_date", nullable = false)
    private LocalDate joinedDate;

    /** Completed years of service (calculated or manually adjusted). */
    @Column(name = "years_of_service", nullable = false, precision = 6, scale = 2)
    private BigDecimal yearsOfService;

    /** Basic salary at time of gratuity calculation. */
    @Column(name = "basic_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal basicSalary;

    /** EmpGratuity amount = (basicSalary / 2) * yearsOfService, or manual override. */
    @Column(name = "gratuity_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal gratuityAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GratuityStatus status = GratuityStatus.DRAFT;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnoreProperties({"role", "createdBy", "modifiedBy", "hibernateLazyInitializer", "handler"})
    private Usr createdBy;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by", nullable = false)
    @JsonIgnoreProperties({"role", "createdBy", "modifiedBy", "hibernateLazyInitializer", "handler"})
    private Usr modifiedBy;

    @UpdateTimestamp
    @Column(name = "modified_date", nullable = false)
    private LocalDateTime modifiedDate;

    /** Set when the row was created via file import; enables import rollback. */
    @Column(name = "import_log_id")
    private Long importLogId;
}
