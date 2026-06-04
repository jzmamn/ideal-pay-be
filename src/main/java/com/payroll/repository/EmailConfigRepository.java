package com.payroll.repository;

import com.payroll.entity.EmailConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailConfigRepository extends JpaRepository<EmailConfig, Long> {
    Optional<EmailConfig> findTopByIsActiveTrueOrderByIdDesc();
}
