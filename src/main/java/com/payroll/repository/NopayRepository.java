package com.payroll.repository;

import com.payroll.entity.Nopay;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NopayRepository extends JpaRepository<Nopay, Long> {

    List<Nopay> findAllByIsActive(Boolean isActive, Sort sort);

    boolean existsByCodeIgnoreCase(String code);

    Optional<Nopay> findByCodeIgnoreCase(String code);
}
