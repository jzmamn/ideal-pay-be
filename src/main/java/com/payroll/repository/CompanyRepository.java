package com.payroll.repository;

import com.payroll.entity.Company;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    boolean existsByCodeIgnoreCase(String code);

    List<Company> findAllByIsActive(Boolean isActive, Sort sort);
}
