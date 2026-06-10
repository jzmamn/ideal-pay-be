package com.payroll.repository;

import com.payroll.entity.EmpGratuity;
import com.payroll.enums.GratuityStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpGratuityRepository extends JpaRepository<EmpGratuity, Long> {

    List<EmpGratuity> findAll(Sort sort);

    List<EmpGratuity> findAllByStatus(GratuityStatus status, Sort sort);

    List<EmpGratuity> findAllByEmployeeId(Long empId, Sort sort);

    long countByCode(String code);
}
