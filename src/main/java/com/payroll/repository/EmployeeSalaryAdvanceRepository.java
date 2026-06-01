package com.payroll.repository;

import com.payroll.entity.EmployeeSalaryAdvance;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeSalaryAdvanceRepository extends JpaRepository<EmployeeSalaryAdvance, Long> {

    List<EmployeeSalaryAdvance> findAllByEmployeeId(Long empId, Sort sort);

    Optional<EmployeeSalaryAdvance> findByEmployeeIdAndPayrollMonth(Long empId, String payrollMonth);

    List<EmployeeSalaryAdvance> findAllByPayrollMonth(String payrollMonth, Sort sort);
}
