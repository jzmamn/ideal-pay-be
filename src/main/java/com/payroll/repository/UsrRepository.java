package com.payroll.repository;

import com.payroll.entity.Usr;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsrRepository extends JpaRepository<Usr, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByUserNameIgnoreCase(String userName);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Usr> findByCode(String code);

    Optional<Usr> findByUserName(String userName);

    List<Usr> findAllByIsActive(Boolean isActive, Sort sort);
}
