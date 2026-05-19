package com.payroll.repository;

import com.payroll.entity.Branch;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    boolean existsByCodeIgnoreCase(String code);

    List<Branch> findAllByIsActive(Boolean isActive, Sort sort);
}
