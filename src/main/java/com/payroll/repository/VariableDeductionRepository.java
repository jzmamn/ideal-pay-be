package com.payroll.repository;

import com.payroll.entity.VariableDeduction;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariableDeductionRepository extends JpaRepository<VariableDeduction, Long> {

    boolean existsByCodeIgnoreCase(String code);

    List<VariableDeduction> findAllByIsActive(Boolean isActive, Sort sort);

    Optional<VariableDeduction> findByCodeIgnoreCase(String code);
}
