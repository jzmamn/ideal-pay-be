package com.payroll.repository;

import com.payroll.entity.EmployeeLoan;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeLoanRepository extends JpaRepository<EmployeeLoan, Long> {
    List<EmployeeLoan> findAllByEmployeeId(Long empId, Sort sort);
    List<EmployeeLoan> findAllByPayrollMonth(String payrollMonth, Sort sort);
    List<EmployeeLoan> findAllByEmployeeIdAndPayrollMonth(Long empId, String payrollMonth);

    // ── Import / export support ──────────────────────────────────────────

    long countByImportLogIdAndIsProcessedTrue(Long importLogId);

    long countByImportLogId(Long importLogId);

    void deleteAllByImportLogId(Long importLogId);

    @Query("select l from EmployeeLoan l join fetch l.employee join fetch l.loan " +
           "where l.payrollMonth = :month order by l.id")
    java.util.stream.Stream<EmployeeLoan> streamAllByPayrollMonth(@Param("month") String month);
}
