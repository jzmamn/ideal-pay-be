package com.payroll.license;
import org.springframework.data.jpa.repository.JpaRepository;
public interface LicenseAuditLogRepository extends JpaRepository<LicenseAuditLog, Long> {}
