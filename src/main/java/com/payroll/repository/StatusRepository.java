package com.payroll.repository;

import com.payroll.entity.Status;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {

    boolean existsByCodeIgnoreCase(String code);

    List<Status> findAllByIsActive(Boolean isActive, Sort sort);
}
