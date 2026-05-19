package com.payroll.repository;

import com.payroll.entity.UserRole;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    boolean existsByCodeIgnoreCase(String code);

    List<UserRole> findAllByIsActive(Boolean isActive, Sort sort);
}
