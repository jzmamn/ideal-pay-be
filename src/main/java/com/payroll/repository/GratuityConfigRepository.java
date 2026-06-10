package com.payroll.repository;

import com.payroll.entity.GratuityConfig;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GratuityConfigRepository extends JpaRepository<GratuityConfig, Long> {

    List<GratuityConfig> findAll(Sort sort);

    List<GratuityConfig> findAllByIsActive(Boolean isActive, Sort sort);

    Optional<GratuityConfig> findFirstByIsActiveTrueOrderByIdAsc();
}
