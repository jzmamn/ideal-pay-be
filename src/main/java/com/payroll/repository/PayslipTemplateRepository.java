package com.payroll.repository;

import com.payroll.entity.PayslipTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayslipTemplateRepository extends JpaRepository<PayslipTemplate, Long> {

    /** Returns one active template used as the default for PDF generation. */
    Optional<PayslipTemplate> findFirstByIsActiveTrue();

    List<PayslipTemplate> findAllByIsActive(Boolean isActive, Sort sort);
}
