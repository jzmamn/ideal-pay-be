package com.payroll.repository;

import com.payroll.entity.UserGroup;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {

    List<UserGroup> findAllByUserId(Long userId, Sort sort);

    List<UserGroup> findAllByGroupId(Long grpId, Sort sort);
}
