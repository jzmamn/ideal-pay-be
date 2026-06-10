package com.payroll.license;

import com.payroll.converter.BooleanToYNConverter;
import com.payroll.entity.Usr;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "software_license")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SoftwareLicense {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "license_id", nullable = false, unique = true, length = 100) private String licenseId;
    @Column(name = "customer_code", nullable = false, length = 10) private String customerCode;
    @Column(name = "customer_name", nullable = false, length = 150) private String customerName;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private LicensePlan plan;
    @Column(name = "employee_limit", nullable = false) private Integer employeeLimit;
    @Column(name = "valid_from", nullable = false) private LocalDate validFrom;
    @Column(name = "valid_till", nullable = false) private LocalDate validTill;
    @Convert(converter = BooleanToYNConverter.class) @Column(name = "maintenance_available", nullable = false) private Boolean maintenanceAvailable;
    @Enumerated(EnumType.STRING) @Column(name = "license_status", nullable = false, length = 30) private LicenseStatus licenseStatus;
    @Lob @Column(name = "raw_license_key", nullable = false, columnDefinition = "MEDIUMTEXT") private String rawLicenseKey;
    @Convert(converter = BooleanToYNConverter.class) @Column(name = "is_current", nullable = false) private Boolean current;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "installed_by", nullable = false) private Usr installedBy;
    @Column(name = "installed_at", nullable = false) private LocalDateTime installedAt;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by", nullable = false) private Usr createdBy;
    @CreationTimestamp @Column(name = "created_date", nullable = false, updatable = false) private LocalDateTime createdDate;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "modified_by", nullable = false) private Usr modifiedBy;
    @UpdateTimestamp @Column(name = "modified_date", nullable = false) private LocalDateTime modifiedDate;
}
