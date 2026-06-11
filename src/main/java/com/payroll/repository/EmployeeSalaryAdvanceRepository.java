package com.payroll.repository;

import com.payroll.entity.EmployeeSalaryAdvance;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeSalaryAdvanceRepository extends JpaRepository<EmployeeSalaryAdvance, Long> {

    List<EmployeeSalaryAdvance> findAllByEmployeeId(Long empId, Sort sort);

    Optional<EmployeeSalaryAdvance> findByEmployeeIdAndPayrollMonth(Long empId, String payrollMonth);

    List<EmployeeSalaryAdvance> findAllByEmployeeIdAndPayrollMonth(Long empId, String payrollMonth);

    List<EmployeeSalaryAdvance> findAllByPayrollMonth(String payrollMonth, Sort sort);

    // ── Import / export support ──────────────────────────────────────────

    long countByImportLogIdAndIsProcessedTrue(Long importLogId);

    long countByImportLogId(Long importLogId);

    void deleteAllByImportLogId(Long importLogId);

    @Query("select s from EmployeeSalaryAdvance s join fetch s.employee " +
           "where s.payrollMonth = :month order by s.id")
    java.util.stream.Stream<EmployeeSalaryAdvance> streamAllByPayrollMonth(@Param("month") String month);
}
