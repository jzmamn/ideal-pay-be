package com.payroll.license;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface SoftwareLicenseRepository extends JpaRepository<SoftwareLicense, Long> {
    Optional<SoftwareLicense> findFirstByCurrentTrue();
    @Modifying @Query("update SoftwareLicense l set l.current = false where l.current = true")
    void clearCurrent();
}
