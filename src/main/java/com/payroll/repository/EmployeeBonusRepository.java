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

    // ── Bonus processing batch ───────────────────────────────────────────

    List<EmployeeBonus> findAllByProcessingBatchId(Long batchId, Sort sort);

    @Query("""
           SELECT eb FROM EmployeeBonus eb
           JOIN FETCH eb.employee e
           LEFT JOIN FETCH e.department
           LEFT JOIN FETCH e.designation
           LEFT JOIN FETCH e.branch
           LEFT JOIN FETCH eb.bonus
           LEFT JOIN FETCH eb.approvedBy
           WHERE eb.processingBatch.id = :batchId
           ORDER BY e.payrollName
           """)
    List<EmployeeBonus> findBatchEntriesWithDetails(@Param("batchId") Long batchId);

    // ── Reports ──────────────────────────────────────────────────────────

    @Query("""
           SELECT eb FROM EmployeeBonus eb
           JOIN FETCH eb.employee e
           LEFT JOIN FETCH e.department
           LEFT JOIN FETCH e.designation
           LEFT JOIN FETCH e.branch
           LEFT JOIN FETCH eb.bonus
           LEFT JOIN FETCH eb.processingBatch pb
           WHERE pb.payrollMonth = :month
           ORDER BY e.payrollName
           """)
    List<EmployeeBonus> findByPayrollMonthWithDetails(@Param("month") String month);

    // ── Import / export support ──────────────────────────────────────────

    long countByImportLogIdAndIsProcessedTrue(Long importLogId);

    long countByImportLogId(Long importLogId);

    void deleteAllByImportLogId(Long importLogId);

    @Query("select b from EmployeeBonus b join fetch b.employee left join fetch b.bonus " +
           "where b.payrollMonth = :month order by b.id")
    java.util.stream.Stream<EmployeeBonus> streamAllByPayrollMonth(@Param("month") String month);
}
