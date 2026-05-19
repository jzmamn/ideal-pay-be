package com.payroll.repository;

import com.payroll.entity.EmpType;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpTypeRepository extends JpaRepository<EmpType, Long> {

    boolean existsByCodeIgnoreCase(String code);

    List<EmpType> findAllByIsActive(Boolean isActive, Sort sort);
}
