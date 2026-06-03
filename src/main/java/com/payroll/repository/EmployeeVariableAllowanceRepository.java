package com.payroll.repository;

import com.payroll.entity.EmployeeVariableAllowance;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeVariableAllowanceRepository extends JpaRepository<EmployeeVariableAllowance, Long> {

    List<EmployeeVariableAllowance> findAllByEmployeeId(Long empId, Sort sort);

    List<EmployeeVariableAllowance> findAllByVariableAllowanceId(Long vaId, Sort sort);

    List<EmployeeVariableAllowance> findAllByEmployeeIdAndPayrollMonth(Long empId, String payrollMonth);

    Optional<EmployeeVariableAllowance> findByEmployee_IdAndVariableAllowance_IdAndPayrollMonth(
            Long employeeId, Long variableAllowanceId, String payrollMonth);
}
