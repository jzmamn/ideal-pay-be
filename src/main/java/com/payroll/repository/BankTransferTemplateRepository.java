package com.payroll.repository;

import com.payroll.entity.BankTransferTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankTransferTemplateRepository extends JpaRepository<BankTransferTemplate, Long> {
    Optional<BankTransferTemplate> findByBank_Id(Long bankId);
    boolean existsByBank_Id(Long bankId);
    List<BankTransferTemplate> findAllByOrderByBankNameAsc();
}
