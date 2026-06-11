package com.payroll.repository;

import com.payroll.entity.ImportSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ImportSessionRepository extends JpaRepository<ImportSession, String> {

    void deleteAllByExpiresAtBefore(LocalDateTime cutoff);
}
