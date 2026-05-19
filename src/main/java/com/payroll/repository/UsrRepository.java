package com.payroll.repository;

import com.payroll.entity.Usr;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsrRepository extends JpaRepository<Usr, Long> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<Usr> findByCode(String code);

    List<Usr> findAllByIsActive(Boolean isActive, Sort sort);
}
