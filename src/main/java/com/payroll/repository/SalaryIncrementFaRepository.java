package com.payroll.repository;

import com.payroll.entity.SalaryIncrementFa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryIncrementFaRepository extends JpaRepository<SalaryIncrementFa, Long> {

    List<SalaryIncrementFa> findAllByDetailId(Long detailId);
}
