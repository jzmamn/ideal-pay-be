package com.payroll.repository;

import com.payroll.entity.EmpPayrollRun;
import com.payroll.enums.PayrollRunStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpPayrollRunRepository extends JpaRepository<EmpPayrollRun, Long> {

    Optional<EmpPayrollRun> findByEmployee_IdAndPayrollMonth(Long empId, String payrollMonth);

    boolean existsByEmployee_IdAndPayrollMonthAndStatus(Long empId, String payrollMonth, PayrollRunStatus status);

    List<EmpPayrollRun> findAllByEmployee_Id(Long empId, Sort sort);

    List<EmpPayrollRun> findAllByPayrollMonth(String payrollMonth, Sort sort);

    List<EmpPayrollRun> findAllByPayrollMonthAndStatus(String payrollMonth, PayrollRunStatus status, Sort sort);

    List<EmpPayrollRun> findAllByParentRunId(Long parentRunId, Sort sort);
}
