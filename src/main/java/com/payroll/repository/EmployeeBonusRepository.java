package com.payroll.repository;

import com.payroll.entity.EmployeeBonus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
