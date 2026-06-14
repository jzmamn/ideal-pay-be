package com.payroll.repository;

import com.payroll.entity.EmployeeLate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeLateRepository extends JpaRepository<EmployeeLate, Long> {

    List<EmployeeLate> findAllByEmployeeId(Long empId, Sort sort);

    List<EmployeeLate> findAllByEmployeeIdAndPayrollMonth(Long empId, String payrollMonth);

    List<EmployeeLate> findAllByPayrollMonth(String payrollMonth, Sort sort);

    /** Legacy single-config lookup — kept for backward compatibility. */
    Optional<EmployeeLate> findByEmployee_IdAndPayrollMonth(Long employeeId, String payrollMonth);

    /** One record per (employee, config, month) — primary multi-config lookup. */
    Optional<EmployeeLate> findByEmployee_IdAndLateConfig_IdAndPayrollMonth(
            Long employeeId, Long lateConfigId, String payrollMonth);

    // ── Import / export support ──────────────────────────────────────────

    long countByImportLogIdAndIsProcessedTrue(Long importLogId);

    long countByImportLogId(Long importLogId);

    void deleteAllByImportLogId(Long importLogId);

    @Query("select l from EmployeeLate l join fetch l.employee " +
           "where l.payrollMonth = :month order by l.id")
    java.util.stream.Stream<EmployeeLate> streamAllByPayrollMonth(@Param("month") String month);
}
