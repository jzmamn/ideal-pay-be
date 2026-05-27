package com.payroll.service.impl;

import com.payroll.dto.response.FormulaFieldDTO;
import com.payroll.repository.FixedAllowanceRepository;
import com.payroll.repository.FixedDeductionRepository;
import com.payroll.repository.OvertimeRepository;
import com.payroll.service.FormulaFieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FormulaFieldServiceImpl implements FormulaFieldService {

    private final FixedAllowanceRepository fixedAllowanceRepository;
    private final FixedDeductionRepository fixedDeductionRepository;
    private final OvertimeRepository overtimeRepository;

    @Override
    public List<FormulaFieldDTO> getAllFormulaFields() {
        List<FormulaFieldDTO> fields = new ArrayList<>();

        // ── Employee Master ───────────────────────────────────────────────────
        fields.add(field("basicSalary", "Basic Salary", "BigDecimal", "Employee",   "Employee's basic salary"));

        // ── Pay Period ────────────────────────────────────────────────────────
        fields.add(field("workingDays",  "Working Days", "Integer",    "Pay Period", "Total payable working days in the period"));
        fields.add(field("nopayDays",    "No-Pay Days",  "Integer",    "Pay Period", "Total unpaid leave days taken"));
        fields.add(field("otHours",      "OT Hours",     "BigDecimal", "Pay Period", "Overtime hours worked in the period"));
        fields.add(field("otRate",       "OT Rate",      "BigDecimal", "Pay Period", "Overtime rate multiplier (e.g. 1.5)"));

        // ── Fixed Allowances (excluding default row) ──────────────────────────
        fixedAllowanceRepository.findByIsActiveTrue().stream()
                .filter(fa -> fa.getId() != -1L)
                .forEach(fa -> fields.add(field(fa.getCode(), fa.getName(), "BigDecimal", "Fixed Allowance",
                        "Fixed allowance amount for " + fa.getName())));

        // ── Fixed Deductions (excluding default row) ──────────────────────────
        fixedDeductionRepository.findAllByIsActive(true, Sort.by("id").ascending()).stream()
                .filter(fd -> fd.getId() != -1L)
                .forEach(fd -> fields.add(field(fd.getCode(), fd.getName(), "BigDecimal", "Fixed Deduction",
                        "Fixed deduction amount for " + fd.getName())));

        // ── Overtime Types (excluding default row) ────────────────────────────
        overtimeRepository.findAllByIsActive(true, Sort.by("id").ascending()).stream()
                .filter(ot -> ot.getId() != -1L)
                .forEach(ot -> fields.add(field(ot.getCode(), ot.getName(), "BigDecimal", "Overtime",
                        "Overtime hours for " + ot.getName())));

        return fields;
    }

    private FormulaFieldDTO field(String variable, String label, String dataType,
                                   String category, String description) {
        return FormulaFieldDTO.builder()
                .variable(variable)
                .label(label)
                .dataType(dataType)
                .category(category)
                .description(description)
                .build();
    }
}
