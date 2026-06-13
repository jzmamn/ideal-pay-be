package com.payroll.repository;

import com.payroll.entity.SystemSetup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SystemSetupRepository extends JpaRepository<SystemSetup, Long> {
    List<SystemSetup> findAllByOrderByCodeAsc();
    Optional<SystemSetup> findByCode(String code);
    List<SystemSetup> findByCodeIn(Collection<String> codes);
}
