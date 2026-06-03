package com.payroll.repository;

import com.payroll.entity.SalaryIncrement;
import com.payroll.entity.SalaryIncrementFa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryIncrementFaRepository extends JpaRepository<SalaryIncrementFa, Long> {

    List<SalaryIncrementFa> findAllByDetailId(Long detailId);

    @Modifying
    @Query("DELETE FROM SalaryIncrementFa fa WHERE fa.detail.id IN " +
           "(SELECT d.id FROM SalaryIncrementDetail d WHERE d.increment = :increment)")
    void deleteAllByIncrement(SalaryIncrement increment);
}
