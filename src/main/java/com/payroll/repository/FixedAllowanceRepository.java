package com.payroll.repository;

import com.payroll.entity.FixedAllowance;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FixedAllowanceRepository extends JpaRepository<FixedAllowance, Long> {

    List<FixedAllowance> findByIsActiveTrue();

    List<FixedAllowance> findAllByIsActive(Boolean isActive, Sort sort);

    boolean existsByCodeIgnoreCase(String code);

    Optional<FixedAllowance> findByCodeIgnoreCase(String code);
}
