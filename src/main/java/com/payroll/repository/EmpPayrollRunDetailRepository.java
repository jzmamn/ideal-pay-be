package com.payroll.repository;

import com.payroll.entity.EmpPayrollRunDetail;
import com.payroll.enums.ComponentType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpPayrollRunDetailRepository extends JpaRepository<EmpPayrollRunDetail, Long> {

    List<EmpPayrollRunDetail> findAllByPayrollRun_Id(Long runId, Sort sort);

    Optional<EmpPayrollRunDetail> findByPayrollRun_IdAndComponentTypeAndComponentId(
            Long runId, ComponentType componentType, Long componentId);
}
