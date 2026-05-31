package com.payroll.repository;

import com.payroll.entity.FixedDeduction;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FixedDeductionRepository extends JpaRepository<FixedDeduction, Long> {

    boolean existsByCodeIgnoreCase(String code);

    List<FixedDeduction> findAllByIsActive(Boolean isActive, Sort sort);

    Optional<FixedDeduction> findByCodeIgnoreCase(String code);
}
