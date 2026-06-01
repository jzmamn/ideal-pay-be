package com.payroll.service.impl;

import com.payroll.dto.request.EmployeeLoanRequestDTO;
import com.payroll.dto.response.EmployeeLoanResponseDTO;
import com.payroll.entity.EmployeeLoan;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.EmployeeLoanMapper;
import com.payroll.repository.EmployeeLoanRepository;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.LoanRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.EmployeeLoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeLoanServiceImpl implements EmployeeLoanService {

    private final EmployeeLoanRepository repository;
    private final EmployeeLoanMapper mapper;
    private final EmployeeRepository employeeRepository;
    private final LoanRepository loanRepository;
    private final UsrRepository usrRepository;

    @Override @Transactional(readOnly = true)
    public List<EmployeeLoanResponseDTO> getAll(boolean showDefaultRow) {
        return repository.findAll(Sort.by("id").ascending()).stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(mapper::toResponseDTO).toList();
    }

    @Override @Transactional(readOnly = true)
    public EmployeeLoanResponseDTO getById(Long id) {
        return mapper.toResponseDTO(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeLoan", "id", id)));
    }

    @Override
    public EmployeeLoanResponseDTO create(EmployeeLoanRequestDTO dto) {
        EmployeeLoan entity = mapper.toEntity(dto);
        setRelationships(entity, dto);
        return mapper.toResponseDTO(repository.save(entity));
    }

    @Override
    public EmployeeLoanResponseDTO update(Long id, EmployeeLoanRequestDTO dto) {
        EmployeeLoan existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeLoan", "id", id));
        mapper.updateEntityFromDTO(dto, existing);
        updateRelationships(existing, dto);
        return mapper.toResponseDTO(repository.save(existing));
    }

    @Override
    public void delete(Long id) {
        repository.delete(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeLoan", "id", id)));
    }

    @Override @Transactional(readOnly = true)
    public List<EmployeeLoanResponseDTO> getByEmployeeId(Long empId) {
        return repository.findAllByEmployeeId(empId, Sort.by("id").ascending())
                .stream().map(mapper::toResponseDTO).toList();
    }

    @Override @Transactional(readOnly = true)
    public List<EmployeeLoanResponseDTO> getByPayrollMonth(String payrollMonth) {
        return repository.findAllByPayrollMonth(payrollMonth, Sort.by("id").ascending())
                .stream().map(mapper::toResponseDTO).toList();
    }

    private void setRelationships(EmployeeLoan e, EmployeeLoanRequestDTO dto) {
        e.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        e.setLoan(loanRepository.getReferenceById(dto.getLoanId()));
        e.setCreatedBy(usrRepository.getReferenceById(dto.getCreatedBy()));
        e.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }

    private void updateRelationships(EmployeeLoan e, EmployeeLoanRequestDTO dto) {
        if (dto.getEmpId()      != null) e.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        if (dto.getLoanId()     != null) e.setLoan(loanRepository.getReferenceById(dto.getLoanId()));
        if (dto.getModifiedBy() != null) e.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }
}
