package com.payroll.repository;

import com.payroll.entity.Type;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TypeRepository extends JpaRepository<Type, Long> {

    boolean existsByCodeIgnoreCase(String code);

    List<Type> findAllByIsActive(Boolean isActive, Sort sort);
}
