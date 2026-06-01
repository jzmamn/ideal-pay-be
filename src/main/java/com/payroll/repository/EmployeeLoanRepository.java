package com.payroll.repository;

import com.payroll.entity.EmployeeLoan;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeLoanRepository extends JpaRepository<EmployeeLoan, Long> {
    List<EmployeeLoan> findAllByEmployeeId(Long empId, Sort sort);
    List<EmployeeLoan> findAllByPayrollMonth(String payrollMonth, Sort sort);
    List<EmployeeLoan> findAllByEmployeeIdAndPayrollMonth(Long empId, String payrollMonth);
}
