package com.payroll.repository;

import com.payroll.entity.EmailTemplate;
import com.payroll.entity.EmailTemplate.TemplateType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    List<EmailTemplate> findAllByOrderByNameAsc();

    List<EmailTemplate> findByIsActiveTrueOrderByNameAsc();

    List<EmailTemplate> findByTemplateTypeAndIsActiveTrueOrderByNameAsc(TemplateType templateType);

    boolean existsByNameAndIdNot(String name, Long id);

    List<EmailTemplate> findByEmailConfigIdOrderByNameAsc(Long emailConfigId);

    List<EmailTemplate> findByEmailConfigIsNullOrderByNameAsc();
}
