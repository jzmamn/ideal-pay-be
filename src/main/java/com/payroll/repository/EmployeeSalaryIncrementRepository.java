package com.payroll.repository;

import com.payroll.entity.EmployeeSalaryIncrement;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeSalaryIncrementRepository extends JpaRepository<EmployeeSalaryIncrement, Long> {
    List<EmployeeSalaryIncrement> findAllByEmployeeId(Long empId, Sort sort);
    List<EmployeeSalaryIncrement> findAllByPayrollMonth(String payrollMonth, Sort sort);
    Optional<EmployeeSalaryIncrement> findByEmployeeIdAndPayrollMonth(Long empId, String payrollMonth);

    // ── Import / export support ──────────────────────────────────────────

    long countByImportLogIdAndIsProcessedTrue(Long importLogId);

    long countByImportLogId(Long importLogId);

    void deleteAllByImportLogId(Long importLogId);

    @Query("select s from EmployeeSalaryIncrement s join fetch s.employee " +
           "where s.payrollMonth = :month order by s.id")
    java.util.stream.Stream<EmployeeSalaryIncrement> streamAllByPayrollMonth(@Param("month") String month);
}
