package com.payroll.repository;

import com.payroll.entity.EmployeeVariableDeduction;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeVariableDeductionRepository extends JpaRepository<EmployeeVariableDeduction, Long> {

    List<EmployeeVariableDeduction> findAllByEmployeeId(Long empId, Sort sort);

    List<EmployeeVariableDeduction> findAllByVariableDeductionId(Long vdId, Sort sort);

    List<EmployeeVariableDeduction> findAllByEmployeeIdAndPayrollMonth(Long empId, String payrollMonth);

    Optional<EmployeeVariableDeduction> findByEmployee_IdAndVariableDeduction_IdAndPayrollMonth(
            Long employeeId, Long variableDeductionId, String payrollMonth);

    // ── Import / export support ──────────────────────────────────────────

    long countByImportLogIdAndIsProcessedTrue(Long importLogId);

    long countByImportLogId(Long importLogId);

    void deleteAllByImportLogId(Long importLogId);

    @Query("select v from EmployeeVariableDeduction v join fetch v.employee join fetch v.variableDeduction " +
           "where v.payrollMonth = :month order by v.id")
    java.util.stream.Stream<EmployeeVariableDeduction> streamAllByPayrollMonth(@Param("month") String month);
}
