package com.payroll.repository;

import com.payroll.entity.EmployeeLate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeLateRepository extends JpaRepository<EmployeeLate, Long> {

    List<EmployeeLate> findAllByEmployeeId(Long empId, Sort sort);

    List<EmployeeLate> findAllByEmployeeIdAndPayrollMonth(Long empId, String payrollMonth);

    Optional<EmployeeLate> findByEmployee_IdAndPayrollMonth(Long employeeId, String payrollMonth);
}
