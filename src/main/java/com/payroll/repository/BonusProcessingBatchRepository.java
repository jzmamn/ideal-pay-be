package com.payroll.repository;

import com.payroll.entity.BonusProcessingBatch;
import com.payroll.enums.BonusStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BonusProcessingBatchRepository extends JpaRepository<BonusProcessingBatch, Long> {

    List<BonusProcessingBatch> findAllByStatus(BonusStatus status, Sort sort);

    List<BonusProcessingBatch> findAllByPayrollMonth(String payrollMonth, Sort sort);

    List<BonusProcessingBatch> findAllByBonusId(Long bonusId, Sort sort);

    @Query("""
           SELECT b FROM BonusProcessingBatch b
           JOIN FETCH b.bonus
           JOIN FETCH b.createdBy
           LEFT JOIN FETCH b.approvedBy
           LEFT JOIN FETCH b.processedBy
           WHERE b.id = :id
           """)
    java.util.Optional<BonusProcessingBatch> findByIdWithDetails(@Param("id") Long id);

    boolean existsByBonusIdAndPayrollMonthAndStatusIn(
            Long bonusId, String payrollMonth, List<BonusStatus> statuses);
}
