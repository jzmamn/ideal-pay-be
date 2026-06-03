package com.payroll.repository;

import com.payroll.entity.Employee;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmployeeNoIgnoreCase(String employeeNo);

    List<Employee> findAllByIsActive(Boolean isActive, Sort sort);

    long countByIsActive(Boolean isActive);
}
