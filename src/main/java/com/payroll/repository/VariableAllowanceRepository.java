package com.payroll.repository;

import com.payroll.entity.VariableAllowance;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VariableAllowanceRepository extends JpaRepository<VariableAllowance, Long> {

    boolean existsByCodeIgnoreCase(String code);

    List<VariableAllowance> findAllByIsActive(Boolean isActive, Sort sort);
}
