package com.payroll.repository;

import com.payroll.entity.EmployeeNopay;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeNopayRepository extends JpaRepository<EmployeeNopay, Long> {

    List<EmployeeNopay> findAllByEmployeeId(Long empId, Sort sort);

    List<EmployeeNopay> findAllByNopayDaysId(Long nopayId, Sort sort);

    List<EmployeeNopay> findAllByEmployeeIdAndPayrollMonth(Long empId, String payrollMonth);

    Optional<EmployeeNopay> findByEmployee_IdAndNopayDays_IdAndPayrollMonth(
            Long employeeId, Long nopayDaysId, String payrollMonth);

    // ── Import / export support ──────────────────────────────────────────

    long countByImportLogIdAndIsProcessedTrue(Long importLogId);

    long countByImportLogId(Long importLogId);

    void deleteAllByImportLogId(Long importLogId);

    @Query("select n from EmployeeNopay n join fetch n.employee join fetch n.nopayDays " +
           "where n.payrollMonth = :month order by n.id")
    java.util.stream.Stream<EmployeeNopay> streamAllByPayrollMonth(@Param("month") String month);
}
