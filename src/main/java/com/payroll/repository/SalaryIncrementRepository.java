package com.payroll.repository;

import com.payroll.entity.SalaryIncrement;
import com.payroll.enums.IncrementStatus;
import com.payroll.enums.IncrementType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryIncrementRepository extends JpaRepository<SalaryIncrement, Long> {

    boolean existsByCodeIgnoreCase(String code);

    List<SalaryIncrement> findAllByType(IncrementType type, Sort sort);

    List<SalaryIncrement> findAllByStatus(IncrementStatus status, Sort sort);

    List<SalaryIncrement> findAllByEffectiveMonth(String effectiveMonth, Sort sort);

    List<SalaryIncrement> findAll(Sort sort);

    long countByEffectiveMonth(String effectiveMonth);
}
