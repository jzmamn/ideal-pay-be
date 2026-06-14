package com.payroll.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.payroll.enums.PayrollRunStatus;
import com.payroll.enums.RunType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "emp_payroll_run",
    uniqueConstraints = @UniqueConstraint(name = "uk_emp_payroll_run", columnNames = {"emp_id", "payroll_month"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpPayrollRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    @JsonIgnoreProperties({"createdBy", "modifiedBy", "hibernateLazyInitializer", "handler"})
    private Employee employee;

    @Column(name = "payroll_month", nullable = false, length = 20)
    private String payrollMonth;

    @Column(name = "basic_salary", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT '0.00'")
    private BigDecimal basicSalary;

    @Column(name = "total_allowances", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT '0.00'")
    private BigDecimal totalAllowances;

    @Column(name = "total_deductions", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT '0.00'")
    private BigDecimal totalDeductions;

    @Column(name = "gross_pay", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT '0.00'")
    private BigDecimal grossPay;

    @Column(name = "net_pay", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT '0.00'")
    private BigDecimal netPay;

    /** Salary base used to compute EPF/ETF (basic + EPF-liable allowances - EPF-liable nopay). */
    @Builder.Default
    @Column(name = "epf_liable_base", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT '0.00'")
    private BigDecimal epfLiableBase = BigDecimal.ZERO;

    /** Taxable earnings derived from component taxable-liability settings. */
    @Builder.Default
    @Column(name = "taxable_earnings", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT '0.00'")
    private BigDecimal taxableEarnings = BigDecimal.ZERO;

    /** Employee EPF contribution — 8% of epfLiableBase (deducted from net pay). */
    @Builder.Default
    @Column(name = "employee_epf", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT '0.00'")
    private BigDecimal employeeEpf = BigDecimal.ZERO;

    /** Employer EPF contribution — 12% of epfLiableBase (company cost, not deducted from employee). */
    @Builder.Default
    @Column(name = "employer_epf", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT '0.00'")
    private BigDecimal employerEpf = BigDecimal.ZERO;

    /** ETF contribution — 3% of epfLiableBase (employer-only). */
    @Builder.Default
    @Column(name = "etf", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT '0.00'")
    private BigDecimal etf = BigDecimal.ZERO;

    /** PAYE income tax deducted from employee. */
    @Builder.Default
    @Column(name = "paye_tax", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) DEFAULT '0.00'")
    private BigDecimal payeTax = BigDecimal.ZERO;

    /** Working days used for this run (sourced from PayrollPeriod). */
    @Builder.Default
    @Column(name = "working_days", nullable = false, columnDefinition = "INT DEFAULT 26")
    private Integer workingDays = 26;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20,
            columnDefinition = "VARCHAR(20) DEFAULT 'DRAFT'")
    private PayrollRunStatus status;

    /** NORMAL for regular payroll runs; CORRECTION for post-lock corrections */
    @Enumerated(EnumType.STRING)
    @Column(name = "run_type", nullable = false, length = 20,
            columnDefinition = "VARCHAR(20) DEFAULT 'NORMAL'")
    @Builder.Default
    private RunType runType = RunType.NORMAL;

    /** For CORRECTION runs: the id of the original LOCKED run being corrected */
    @Column(name = "parent_run_id")
    private Long parentRunId;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    @JsonIgnoreProperties({"role", "createdBy", "modifiedBy", "hibernateLazyInitializer", "handler"})
    private Usr processedBy;

    @OneToMany(mappedBy = "payrollRun", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<EmpPayrollRunDetail> details = new ArrayList<>();

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
