package com.payroll.repository;

import com.payroll.entity.Bonus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BonusRepository extends JpaRepository<Bonus, Long> {

    List<Bonus> findAllByIsActive(Boolean isActive, Sort sort);
}
