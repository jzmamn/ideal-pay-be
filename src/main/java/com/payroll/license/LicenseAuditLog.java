package com.payroll.license;

import com.payroll.entity.Usr;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "license_audit_log")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class LicenseAuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "license_id", length = 100) private String licenseId;
    @Column(nullable = false, length = 50) private String action;
    @Column(nullable = false, length = 30) private String status;
    @Column(length = 1000) private String message;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "performed_by", nullable = false) private Usr performedBy;
    @CreationTimestamp @Column(name = "created_date", nullable = false, updatable = false) private LocalDateTime createdDate;
}
