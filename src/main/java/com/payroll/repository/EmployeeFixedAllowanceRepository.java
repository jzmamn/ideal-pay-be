package com.payroll.repository;

import com.payroll.entity.EmployeeFixedAllowance;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeFixedAllowanceRepository extends JpaRepository<EmployeeFixedAllowance, Long> {

    List<EmployeeFixedAllowance> findAllByEmployeeId(Long empId, Sort sort);

    List<EmployeeFixedAllowance> findAllByFixedAllowanceId(Long faId, Sort sort);
}
