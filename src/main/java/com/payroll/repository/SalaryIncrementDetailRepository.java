package com.payroll.repository;

import com.payroll.entity.SalaryIncrement;
import com.payroll.entity.SalaryIncrementDetail;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryIncrementDetailRepository extends JpaRepository<SalaryIncrementDetail, Long> {

    List<SalaryIncrementDetail> findAllByIncrementId(Long incrementId, Sort sort);

    List<SalaryIncrementDetail> findAllByEmployeeId(Long empId, Sort sort);

    boolean existsByIncrementIdAndEmployeeId(Long incrementId, Long empId);

    @Modifying
    @Query("DELETE FROM SalaryIncrementDetail d WHERE d.increment = :increment")
    void deleteAllByIncrement(SalaryIncrement increment);
}
