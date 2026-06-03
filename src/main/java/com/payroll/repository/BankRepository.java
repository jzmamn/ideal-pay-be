package com.payroll.repository;

import com.payroll.entity.Bank;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {

    boolean existsByCodeIgnoreCase(String code);

    List<Bank> findAllByIsActive(Boolean isActive, Sort sort);
}
