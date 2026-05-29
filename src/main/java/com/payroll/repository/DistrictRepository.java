package com.payroll.repository;

import com.payroll.entity.District;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<District> findAllByIsActive(Boolean isActive, Sort sort);
}
