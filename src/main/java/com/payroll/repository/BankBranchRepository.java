package com.payroll.repository;

import com.payroll.entity.BankBranch;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankBranchRepository extends JpaRepository<BankBranch, Long> {

    List<BankBranch> findAllByBankId(Long bankId, Sort sort);

    List<BankBranch> findAllByIsActive(Boolean isActive, Sort sort);

    boolean existsByBankIdAndBranchCodeIgnoreCase(Long bankId, String branchCode);
}
