package com.payroll.repository;

import com.payroll.entity.LateDeductionConfig;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LateDeductionConfigRepository extends JpaRepository<LateDeductionConfig, Long> {

    List<LateDeductionConfig> findAllByIsActive(Boolean isActive, Sort sort);

    /** Returns the first active config, used by the calculation engine. */
    Optional<LateDeductionConfig> findFirstByIsActiveTrueOrderByIdAsc();
}
