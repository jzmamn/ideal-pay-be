package com.payroll.repository;

import com.payroll.entity.NopayDays;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NopayDaysRepository extends JpaRepository<NopayDays, Long> {

    boolean existsByCodeIgnoreCase(String code);

    List<NopayDays> findAllByIsActive(Boolean isActive, Sort sort);

    Optional<NopayDays> findByCodeIgnoreCase(String code);
}
