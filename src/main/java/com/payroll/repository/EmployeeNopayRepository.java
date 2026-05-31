package com.payroll.repository;

import com.payroll.entity.EmployeeNopay;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeNopayRepository extends JpaRepository<EmployeeNopay, Long> {

    List<EmployeeNopay> findAllByEmployeeId(Long empId, Sort sort);

    List<EmployeeNopay> findAllByNopayDaysId(Long nopayId, Sort sort);

    List<EmployeeNopay> findAllByEmployeeIdAndPayrollMonth(Long empId, String payrollMonth);
}
