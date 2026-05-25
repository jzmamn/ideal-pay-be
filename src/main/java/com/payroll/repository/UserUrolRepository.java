package com.payroll.repository;

import com.payroll.entity.UserUrol;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserUrolRepository extends JpaRepository<UserUrol, Long> {

    List<UserUrol> findAllByUserId(Long userId, Sort sort);

    List<UserUrol> findAllByUrolId(Long urolId, Sort sort);
}
