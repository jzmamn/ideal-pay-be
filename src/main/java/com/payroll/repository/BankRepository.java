package com.payroll.repository;

import com.payroll.entity.Bank;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCode(String code);

    Optional<Bank> findByCode(String code);

    List<Bank> findAllByIsActive(Boolean isActive, Sort sort);
}
