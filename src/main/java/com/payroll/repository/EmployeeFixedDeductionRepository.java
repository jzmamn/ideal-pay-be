package com.payroll.repository;

import com.payroll.entity.EmployeeFixedDeduction;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeFixedDeductionRepository extends JpaRepository<EmployeeFixedDeduction, Long> {

    List<EmployeeFixedDeduction> findAllByEmployeeId(Long empId, Sort sort);

    List<EmployeeFixedDeduction> findAllByFixedDeductionId(Long fdId, Sort sort);

    List<EmployeeFixedDeduction> findAllByEmployeeIdAndPayrollMonth(Long empId, String payrollMonth);
}
