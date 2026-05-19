package com.payroll.repository;

import com.payroll.entity.FixedDeduction;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FixedDeductionRepository extends JpaRepository<FixedDeduction, Long> {

    boolean existsByCodeIgnoreCase(String code);

    List<FixedDeduction> findAllByIsActive(Boolean isActive, Sort sort);
}
