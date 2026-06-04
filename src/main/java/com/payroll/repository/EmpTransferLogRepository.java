package com.payroll.repository;

import com.payroll.entity.EmpTransferLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpTransferLogRepository extends JpaRepository<EmpTransferLog, Long> {

    boolean existsByPayrollRun_IdAndTransferType(Long runId, String transferType);

    List<EmpTransferLog> findAllByPayrollRun_IdIn(List<Long> runIds);
}
