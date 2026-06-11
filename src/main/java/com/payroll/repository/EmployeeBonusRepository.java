package com.payroll.repository;

import com.payroll.entity.EmployeeBonus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeBonusRepository extends JpaRepository<EmployeeBonus, Long> {
    List<EmployeeBonus> findAllByEmployeeId(Long empId, Sort sort);
    List<EmployeeBonus> findAllByPayrollMonth(String payrollMonth, Sort sort);
    Optional<EmployeeBonus> findByEmployeeIdAndPayrollMonth(Long empId, String payrollMonth);
    List<EmployeeBonus> findAllByEmployeeIdAndPayrollMonth(Long empId, String payrollMonth);
    Optional<EmployeeBonus> findByEmployeeIdAndPayrollMonthAndBonusId(Long empId, String payrollMonth, Long bonusId);

    // ── Import / export support ──────────────────────────────────────────

    long countByImportLogIdAndIsProcessedTrue(Long importLogId);

    long countByImportLogId(Long importLogId);

    void deleteAllByImportLogId(Long importLogId);

    @Query("select b from EmployeeBonus b join fetch b.employee left join fetch b.bonus " +
           "where b.payrollMonth = :month order by b.id")
    java.util.stream.Stream<EmployeeBonus> streamAllByPayrollMonth(@Param("month") String month);
}
