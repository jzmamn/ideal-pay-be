package com.payroll.repository;

import com.payroll.entity.JobCategory;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobCategoryRepository extends JpaRepository<JobCategory, Long> {

    boolean existsByCodeIgnoreCase(String code);

    List<JobCategory> findAllByIsActive(Boolean isActive, Sort sort);
}
