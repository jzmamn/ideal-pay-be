package com.payroll.repository;

import com.payroll.entity.PayrollPeriod;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollPeriodRepository extends JpaRepository<PayrollPeriod, Long> {

    List<PayrollPeriod> findAll(Sort sort);

    List<PayrollPeriod> findAllByCompany_Id(Long companyId, Sort sort);

    Optional<PayrollPeriod> findByCompany_IdAndActive(Long companyId, Boolean active);

    List<PayrollPeriod> findAllByCompany_IdAndActive(Long companyId, Boolean active);

    Optional<PayrollPeriod> findByCompany_IdAndPeriodYearAndPeriodMonth(
            Long companyId, Integer periodYear, Integer periodMonth);

    boolean existsByCompany_IdAndPeriodYearAndPeriodMonth(
            Long companyId, Integer periodYear, Integer periodMonth);

    boolean existsByCompany_IdAndPeriodYearAndPeriodMonthAndIdNot(
            Long companyId, Integer periodYear, Integer periodMonth, Long id);

    /** All periods sharing a YYYY-MM code (across companies) — legacy compatibility. */
    List<PayrollPeriod> findAllByPeriodCode(String periodCode);

    Optional<PayrollPeriod> findByCompany_IdAndPeriodCode(Long companyId, String periodCode);
}
