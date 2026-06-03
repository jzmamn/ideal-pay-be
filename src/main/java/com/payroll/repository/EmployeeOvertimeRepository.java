package com.payroll.repository;

import com.payroll.entity.EmployeeOvertime;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeOvertimeRepository extends JpaRepository<EmployeeOvertime, Long> {

    List<EmployeeOvertime> findAllByEmployeeId(Long empId, Sort sort);

    List<EmployeeOvertime> findAllByOvertimeId(Long overtimeId, Sort sort);

    List<EmployeeOvertime> findAllByEmployeeIdAndPayrollMonth(Long empId, String payrollMonth);

    Optional<EmployeeOvertime> findByEmployee_IdAndOvertime_IdAndPayrollMonth(
            Long employeeId, Long overtimeId, String payrollMonth);
}
