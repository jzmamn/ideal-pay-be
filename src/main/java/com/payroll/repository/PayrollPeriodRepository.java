package com.payroll.repository;

import com.payroll.entity.PayrollPeriod;
import com.payroll.enums.PeriodStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollPeriodRepository extends JpaRepository<PayrollPeriod, Long> {

    Optional<PayrollPeriod> findByPeriodMonth(String periodMonth);

    boolean existsByPeriodMonth(String periodMonth);

    List<PayrollPeriod> findAllByStatus(PeriodStatus status, Sort sort);

    List<PayrollPeriod> findAll(Sort sort);
}
