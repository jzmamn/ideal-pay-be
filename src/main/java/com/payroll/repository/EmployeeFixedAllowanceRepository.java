package com.payroll.repository;

import com.payroll.entity.EmployeeFixedAllowance;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeFixedAllowanceRepository extends JpaRepository<EmployeeFixedAllowance, Long> {

    List<EmployeeFixedAllowance> findAllByEmployeeId(Long empId, Sort sort);

    List<EmployeeFixedAllowance> findAllByFixedAllowanceId(Long faId, Sort sort);

    List<EmployeeFixedAllowance> findAllByEmployeeIdAndPayrollMonth(Long empId, String payrollMonth);

    Optional<EmployeeFixedAllowance> findByEmployee_IdAndFixedAllowance_IdAndPayrollMonth(
            Long employeeId, Long fixedAllowanceId, String payrollMonth);
}
