package com.payroll.repository;

import com.payroll.entity.EmployeeOvertime;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeOvertimeRepository extends JpaRepository<EmployeeOvertime, Long> {

    List<EmployeeOvertime> findAllByEmployeeId(Long empId, Sort sort);

    List<EmployeeOvertime> findAllByOvertimeId(Long overtimeId, Sort sort);

    List<EmployeeOvertime> findAllByEmployeeIdAndPayrollMonth(Long empId, String payrollMonth);

    Optional<EmployeeOvertime> findByEmployee_IdAndOvertime_IdAndPayrollMonth(
            Long employeeId, Long overtimeId, String payrollMonth);

    // ── Import / export support ──────────────────────────────────────────

    long countByImportLogIdAndIsProcessedTrue(Long importLogId);

    long countByImportLogId(Long importLogId);

    void deleteAllByImportLogId(Long importLogId);

    @Query("select o from EmployeeOvertime o join fetch o.employee join fetch o.overtime " +
           "where o.payrollMonth = :month order by o.id")
    java.util.stream.Stream<EmployeeOvertime> streamAllByPayrollMonth(@Param("month") String month);
}
