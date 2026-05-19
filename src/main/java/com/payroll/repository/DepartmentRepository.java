package com.payroll.repository;

import com.payroll.entity.Department;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByCodeIgnoreCase(String code);

    List<Department> findAllByIsActive(Boolean isActive, Sort sort);
}
