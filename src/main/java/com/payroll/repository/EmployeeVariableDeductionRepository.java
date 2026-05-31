package com.payroll.repository;

import com.payroll.entity.EmployeeVariableDeduction;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeVariableDeductionRepository extends JpaRepository<EmployeeVariableDeduction, Long> {

    List<EmployeeVariableDeduction> findAllByEmployeeId(Long empId, Sort sort);

    List<EmployeeVariableDeduction> findAllByVariableDeductionId(Long vdId, Sort sort);

    List<EmployeeVariableDeduction> findAllByEmployeeIdAndPayrollMonth(Long empId, String payrollMonth);
}
