package com.payroll.repository;

import com.payroll.entity.Overtime;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OvertimeRepository extends JpaRepository<Overtime, Long> {

    boolean existsByCodeIgnoreCase(String code);

    List<Overtime> findAllByIsActive(Boolean isActive, Sort sort);

    Optional<Overtime> findByCodeIgnoreCase(String code);
}
