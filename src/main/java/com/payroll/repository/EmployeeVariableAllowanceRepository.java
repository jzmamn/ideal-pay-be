package com.payroll.repository;

import com.payroll.entity.EmployeeVariableAllowance;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // ── Import / export support ──────────────────────────────────────────

    long countByImportLogIdAndIsProcessedTrue(Long importLogId);

    long countByImportLogId(Long importLogId);

    void deleteAllByImportLogId(Long importLogId);

    @Query("select v from EmployeeVariableAllowance v join fetch v.employee join fetch v.variableAllowance " +
           "where v.payrollMonth = :month order by v.id")
    java.util.stream.Stream<EmployeeVariableAllowance> streamAllByPayrollMonth(@Param("month") String month);
}
