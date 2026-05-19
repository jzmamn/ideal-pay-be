package com.payroll.repository;

import com.payroll.entity.VariableDeduction;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VariableDeductionRepository extends JpaRepository<VariableDeduction, Long> {

    boolean existsByCodeIgnoreCase(String code);

    List<VariableDeduction> findAllByIsActive(Boolean isActive, Sort sort);
}
