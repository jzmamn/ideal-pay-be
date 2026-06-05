package com.payroll.repository;

import com.payroll.entity.EmailConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmailConfigRepository extends JpaRepository<EmailConfig, Long> {

    List<EmailConfig> findAllByOrderByIdDesc();

    Optional<EmailConfig> findTopByIsActiveTrueOrderByIdDesc();

    boolean existsByNameAndIdNot(String name, Long id);

    /** Deactivate every config in one shot before activating a specific one. */
    @Modifying
    @Query("UPDATE EmailConfig e SET e.isActive = false")
    void deactivateAll();
}
