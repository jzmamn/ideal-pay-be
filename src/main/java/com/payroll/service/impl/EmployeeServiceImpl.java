package com.payroll.service.impl;

import com.payroll.dto.request.EmployeeRequestDTO;
import com.payroll.dto.response.EmployeeResponseDTO;
import com.payroll.entity.Employee;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.EmployeeMapper;
import com.payroll.repository.*;
import com.payroll.repository.TypeRepository;
import com.payroll.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final TypeRepository typeRepository;
    private final NopayDaysRepository nopayDaysRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final DesignationRepository designationRepository;
    private final BranchRepository branchRepository;
    private final GradeRepository gradeRepository;
    private final StatusRepository statusRepository;
    private final CountryRepository countryRepository;
    private final UsrRepository usrRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getAllEmployees(boolean showDefaultRow, String isActive) {
        if (!isActive.equalsIgnoreCase("true") && !isActive.equalsIgnoreCase("false") && !isActive.equalsIgnoreCase("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<Employee> records = "all".equals(isActive)
                ? employeeRepository.findAll(sort)
                : employeeRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(employeeMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return employeeMapper.toResponseDTO(employee);
    }

    @Override
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO requestDTO) {
        if (employeeRepository.existsByEmployeeNoIgnoreCase(requestDTO.getEmployeeNo())) {
            throw new IllegalArgumentException(
                    "An employee with number '" + requestDTO.getEmployeeNo() + "' already exists.");
        }
        Employee entity = employeeMapper.toEntity(requestDTO);
        setRelationships(entity, requestDTO);
        return employeeMapper.toResponseDTO(employeeRepository.save(entity));
    }

    @Override
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO requestDTO) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        employeeMapper.updateEntityFromDTO(requestDTO, existing);
        updateRelationships(existing, requestDTO);
        return employeeMapper.toResponseDTO(employeeRepository.save(existing));
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        employeeRepository.delete(employee);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void setRelationships(Employee entity, EmployeeRequestDTO dto) {
        entity.setEmployeeType(typeRepository.getReferenceById(dto.getEmployeeTypeId()));
        entity.setNopayDays(nopayDaysRepository.getReferenceById(dto.getNopayDaysId()));
        entity.setJobCategory(jobCategoryRepository.getReferenceById(dto.getJobCategoryId()));
        entity.setDesignation(designationRepository.getReferenceById(dto.getDesignationId()));
        entity.setBranch(branchRepository.getReferenceById(dto.getBranchId()));
        entity.setGrade(gradeRepository.getReferenceById(dto.getGradeId()));
        entity.setStatus(statusRepository.getReferenceById(dto.getStatusId()));
        entity.setCountry(countryRepository.getReferenceById(dto.getCountryId()));
        entity.setCreatedBy(usrRepository.getReferenceById(dto.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }

    private void updateRelationships(Employee entity, EmployeeRequestDTO dto) {
        if (dto.getEmployeeTypeId() != null)
            entity.setEmployeeType(typeRepository.getReferenceById(dto.getEmployeeTypeId()));
        if (dto.getNopayDaysId() != null)
            entity.setNopayDays(nopayDaysRepository.getReferenceById(dto.getNopayDaysId()));
        if (dto.getJobCategoryId() != null)
            entity.setJobCategory(jobCategoryRepository.getReferenceById(dto.getJobCategoryId()));
        if (dto.getDesignationId() != null)
            entity.setDesignation(designationRepository.getReferenceById(dto.getDesignationId()));
        if (dto.getBranchId() != null)
            entity.setBranch(branchRepository.getReferenceById(dto.getBranchId()));
        if (dto.getGradeId() != null)
            entity.setGrade(gradeRepository.getReferenceById(dto.getGradeId()));
        if (dto.getStatusId() != null)
            entity.setStatus(statusRepository.getReferenceById(dto.getStatusId()));
        if (dto.getCountryId() != null)
            entity.setCountry(countryRepository.getReferenceById(dto.getCountryId()));
        if (dto.getModifiedBy() != null)
            entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }
}
