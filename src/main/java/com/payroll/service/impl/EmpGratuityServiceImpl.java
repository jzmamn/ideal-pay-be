package com.payroll.service.impl;

import com.payroll.dto.request.EmpGratuityRequest;
import com.payroll.dto.response.EmpGratuityResponse;
import com.payroll.entity.Employee;
import com.payroll.entity.EmpGratuity;
import com.payroll.entity.Usr;
import com.payroll.enums.GratuityStatus;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.EmpGratuityMapper;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.EmpGratuityRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.EmpGratuityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpGratuityServiceImpl implements EmpGratuityService {

    private final EmpGratuityRepository  gratuityRepo;
    private final EmployeeRepository  employeeRepo;
    private final UsrRepository       usrRepo;
    private final EmpGratuityMapper      mapper;

    private static final Sort ID_DESC = Sort.by("id").descending();

    // ── Queries ────────────────────────────────────────────────────────────────

    @Override
    public List<EmpGratuityResponse> getAll() {
        return gratuityRepo.findAll(ID_DESC).stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<EmpGratuityResponse> getByStatus(GratuityStatus status) {
        return gratuityRepo.findAllByStatus(status, ID_DESC).stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<EmpGratuityResponse> getByEmployee(Long empId) {
        return gratuityRepo.findAllByEmployeeId(empId, ID_DESC).stream().map(mapper::toResponse).toList();
    }

    @Override
    public EmpGratuityResponse getById(Long id) {
        return mapper.toResponse(findGratuity(id));
    }

    @Override
    public String nextCode() {
        long seq = gratuityRepo.count() + 1;
        return String.format("GT-%05d", seq);
    }

    // ── Mutations ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public EmpGratuityResponse create(EmpGratuityRequest req) {
        Employee emp       = findEmployee(req.getEmpId());
        Usr      createdBy = findUser(req.getCreatedBy());
        Usr      modifiedBy = findUser(req.getModifiedBy());

        EmpGratuity gratuity = EmpGratuity.builder()
                .code(nextCode())
                .employee(emp)
                .terminationDate(req.getTerminationDate())
                .joinedDate(req.getJoinedDate())
                .yearsOfService(req.getYearsOfService())
                .basicSalary(req.getBasicSalary())
                .gratuityAmount(req.getGratuityAmount())
                .status(GratuityStatus.DRAFT)
                .remarks(req.getRemarks())
                .createdBy(createdBy)
                .modifiedBy(modifiedBy)
                .build();

        return mapper.toResponse(gratuityRepo.save(gratuity));
    }

    @Override
    @Transactional
    public EmpGratuityResponse update(Long id, EmpGratuityRequest req) {
        EmpGratuity gratuity = findGratuity(id);
        assertEditable(gratuity);

        Employee emp = findEmployee(req.getEmpId());
        Usr      modifiedBy = findUser(req.getModifiedBy());

        gratuity.setEmployee(emp);
        gratuity.setTerminationDate(req.getTerminationDate());
        gratuity.setJoinedDate(req.getJoinedDate());
        gratuity.setYearsOfService(req.getYearsOfService());
        gratuity.setBasicSalary(req.getBasicSalary());
        gratuity.setGratuityAmount(req.getGratuityAmount());
        gratuity.setRemarks(req.getRemarks());
        gratuity.setModifiedBy(modifiedBy);

        return mapper.toResponse(gratuityRepo.save(gratuity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        EmpGratuity gratuity = findGratuity(id);
        assertEditable(gratuity);
        gratuityRepo.delete(gratuity);
    }

    // ── Workflow ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public EmpGratuityResponse approve(Long id) {
        EmpGratuity g = findGratuity(id);
        if (g.getStatus() != GratuityStatus.DRAFT)
            throw new IllegalStateException("Only DRAFT gratuity can be approved.");
        g.setStatus(GratuityStatus.APPROVED);
        return mapper.toResponse(gratuityRepo.save(g));
    }

    @Override
    @Transactional
    public EmpGratuityResponse markPaid(Long id) {
        EmpGratuity g = findGratuity(id);
        if (g.getStatus() != GratuityStatus.APPROVED)
            throw new IllegalStateException("Only APPROVED gratuity can be marked as paid.");
        g.setStatus(GratuityStatus.PAID);
        return mapper.toResponse(gratuityRepo.save(g));
    }

    @Override
    @Transactional
    public EmpGratuityResponse cancel(Long id) {
        EmpGratuity g = findGratuity(id);
        if (g.getStatus() == GratuityStatus.PAID)
            throw new IllegalStateException("PAID gratuity cannot be cancelled.");
        g.setStatus(GratuityStatus.CANCELLED);
        return mapper.toResponse(gratuityRepo.save(g));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private EmpGratuity findGratuity(Long id) {
        return gratuityRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmpGratuity", "id", id));
    }

    private Employee findEmployee(Long id) {
        return employeeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }

    private Usr findUser(Long id) {
        return usrRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    private void assertEditable(EmpGratuity g) {
        if (g.getStatus() == GratuityStatus.PAID || g.getStatus() == GratuityStatus.CANCELLED)
            throw new IllegalStateException("Cannot modify a " + g.getStatus() + " gratuity.");
    }
}
