package com.payroll.repository;

import com.payroll.entity.EmployeeFixedAllowance;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeFixedAllowanceRepository extends JpaRepository<EmployeeFixedAllowance, Long> {

    List<EmployeeFixedAllowance> findAllByEmployeeId(Long empId, Sort sort);

    List<EmployeeFixedAllowance> findAllByFixedAllowanceId(Long faId, Sort sort);

    List<EmployeeFixedAllowance> findAllByEmployeeIdAndPayrollMonth(Long empId, String payrollMonth);

    Optional<EmployeeFixedAllowance> findByEmployee_IdAndFixedAllowance_IdAndPayrollMonth(
            Long employeeId, Long fixedAllowanceId, String payrollMonth);

    /**
     * True when the employee has been assigned this fixed allowance for at least one
     * payroll month (i.e. the allowance was explicitly selected via the Employee
     * Salary tab at some point). Used to gate automatic carry-forward during batch
     * component load — allowances the employee never selected are never auto-created.
     */
    boolean existsByEmployee_IdAndFixedAllowance_Id(Long employeeId, Long fixedAllowanceId);

    // ── Import / export support ──────────────────────────────────────────

    long countByImportLogIdAndIsProcessedTrue(Long importLogId);

    long countByImportLogId(Long importLogId);

    void deleteAllByImportLogId(Long importLogId);

    @Query("select f from EmployeeFixedAllowance f join fetch f.employee join fetch f.fixedAllowance " +
           "where f.payrollMonth = :month order by f.id")
    java.util.stream.Stream<EmployeeFixedAllowance> streamAllByPayrollMonth(@Param("month") String month);
}
