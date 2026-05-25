package com.payroll.repository;

import com.payroll.entity.Grp;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrpRepository extends JpaRepository<Grp, Long> {

    List<Grp> findAllByIsActive(Boolean isActive, Sort sort);

    List<Grp> findByIsActiveTrue();
}
