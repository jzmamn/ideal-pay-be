package com.payroll.repository;

import com.payroll.entity.Grade;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    boolean existsByCodeIgnoreCase(String code);

    List<Grade> findAllByIsActive(Boolean isActive, Sort sort);
}
