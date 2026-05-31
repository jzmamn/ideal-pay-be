package com.payroll.repository;

import com.payroll.entity.VariableAllowance;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariableAllowanceRepository extends JpaRepository<VariableAllowance, Long> {

    boolean existsByCodeIgnoreCase(String code);

    List<VariableAllowance> findAllByIsActive(Boolean isActive, Sort sort);

    Optional<VariableAllowance> findByCodeIgnoreCase(String code);
}
