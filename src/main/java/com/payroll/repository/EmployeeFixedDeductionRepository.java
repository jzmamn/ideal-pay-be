package com.payroll.repository;

import com.payroll.entity.EmployeeFixedDeduction;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeFixedDeductionRepository extends JpaRepository<EmployeeFixedDeduction, Long> {

    List<EmployeeFixedDeduction> findAllByEmployeeId(Long empId, Sort sort);

    List<EmployeeFixedDeduction> findAllByFixedDeductionId(Long fdId, Sort sort);

    List<EmployeeFixedDeduction> findAllByEmployeeIdAndPayrollMonth(Long empId, String payrollMonth);

    Optional<EmployeeFixedDeduction> findByEmployee_IdAndFixedDeduction_IdAndPayrollMonth(
            Long employeeId, Long fixedDeductionId, String payrollMonth);

    /**
     * True when the employee has been assigned this fixed deduction for at least one
     * payroll month (i.e. the deduction was explicitly selected via the Employee
     * Salary tab at some point). Used to gate automatic carry-forward during batch
     * component load — deductions the employee never selected are never auto-created.
     */
    boolean existsByEmployee_IdAndFixedDeduction_Id(Long employeeId, Long fixedDeductionId);

    // ── Import / export support ──────────────────────────────────────────

    long countByImportLogIdAndIsProcessedTrue(Long importLogId);

    long countByImportLogId(Long importLogId);

    void deleteAllByImportLogId(Long importLogId);

    @Query("select f from EmployeeFixedDeduction f join fetch f.employee join fetch f.fixedDeduction " +
           "where f.payrollMonth = :month order by f.id")
    java.util.stream.Stream<EmployeeFixedDeduction> streamAllByPayrollMonth(@Param("month") String month);
}
