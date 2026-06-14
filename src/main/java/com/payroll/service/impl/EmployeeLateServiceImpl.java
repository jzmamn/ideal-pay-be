package com.payroll.service.impl;

import com.payroll.dto.request.EmployeeLateRequestDTO;
import com.payroll.dto.response.EmployeeLateResponseDTO;
import com.payroll.entity.EmployeeLate;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.EmployeeLateMapper;
import com.payroll.repository.EmployeeLateRepository;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.LateDeductionConfigRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.EmployeeLateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeLateServiceImpl implements EmployeeLateService {

    private final EmployeeLateRepository       employeeLateRepository;
    private final EmployeeLateMapper           employeeLateMapper;
    private final EmployeeRepository           employeeRepository;
    private final LateDeductionConfigRepository lateConfigRepository;
    private final UsrRepository                usrRepository;

    private static final Sort DEFAULT_SORT = Sort.by("id").ascending();

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeLateResponseDTO> getAllEmployeeLates(boolean showDefaultRow) {
        return employeeLateRepository.findAll(DEFAULT_SORT).stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(employeeLateMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeLateResponseDTO getEmployeeLateById(Long id) {
        EmployeeLate entity = employeeLateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeLate", "id", id));
        return employeeLateMapper.toResponseDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeLateResponseDTO> getByPayrollMonth(String payrollMonth) {
        return employeeLateRepository.findAllByPayrollMonth(payrollMonth, DEFAULT_SORT).stream()
                .filter(e -> e.getId() != -1L)
                .map(employeeLateMapper::toResponseDTO)
                .toList();
    }

    /**
     * Creates or updates an EmployeeLate record for a given (employee, config, month) triplet.
     * The rate field is never overwritten here — it is set exclusively by the load phase.
     * The amount is always recalculated as rate × hours to maintain consistency.
     */
    @Override
    public EmployeeLateResponseDTO createEmployeeLate(EmployeeLateRequestDTO requestDTO) {
        EmployeeLate entity = resolveExisting(requestDTO);

        if (entity != null) {
            // Update only the user-editable fields; rate is owned by the load phase.
            BigDecimal hours = orZero(requestDTO.getHours());
            entity.setHours(hours);
            entity.setAmount(computeAmount(entity.getRate(), hours));
            entity.setIsProcessed(requestDTO.getIsProcessed() != null
                    ? requestDTO.getIsProcessed() : Boolean.FALSE);
            entity.setProcessedDate(requestDTO.getProcessedDate());
            entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        } else {
            entity = employeeLateMapper.toEntity(requestDTO);
            setRelationships(entity, requestDTO);
            // Rate defaults to ZERO until the load phase sets it.
            BigDecimal hours = orZero(requestDTO.getHours());
            entity.setAmount(computeAmount(entity.getRate(), hours));
        }

        return employeeLateMapper.toResponseDTO(employeeLateRepository.save(entity));
    }

    @Override
    public EmployeeLateResponseDTO updateEmployeeLate(Long id, EmployeeLateRequestDTO requestDTO) {
        EmployeeLate existing = employeeLateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeLate", "id", id));

        // Only hours is user-editable; amount is always rate × hours.
        BigDecimal hours = orZero(requestDTO.getHours());
        existing.setHours(hours);
        existing.setAmount(computeAmount(existing.getRate(), hours));

        if (requestDTO.getIsProcessed() != null)
            existing.setIsProcessed(requestDTO.getIsProcessed());
        if (requestDTO.getProcessedDate() != null)
            existing.setProcessedDate(requestDTO.getProcessedDate());
        if (requestDTO.getModifiedBy() != null)
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));

        return employeeLateMapper.toResponseDTO(employeeLateRepository.save(existing));
    }

    @Override
    public void deleteEmployeeLate(Long id) {
        EmployeeLate entity = employeeLateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeLate", "id", id));
        employeeLateRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeLateResponseDTO> getByEmployeeId(Long empId) {
        return employeeLateRepository.findAllByEmployeeId(empId, DEFAULT_SORT).stream()
                .map(employeeLateMapper::toResponseDTO)
                .toList();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    /**
     * Looks up an existing record for this request. Prefers the (emp, config, month) triplet
     * when a lateConfigId is provided; falls back to (emp, month) for legacy callers.
     */
    private EmployeeLate resolveExisting(EmployeeLateRequestDTO dto) {
        if (dto.getLateConfigId() != null) {
            return employeeLateRepository
                    .findByEmployee_IdAndLateConfig_IdAndPayrollMonth(
                            dto.getEmpId(), dto.getLateConfigId(), dto.getPayrollMonth())
                    .orElse(null);
        }
        return employeeLateRepository
                .findByEmployee_IdAndPayrollMonth(dto.getEmpId(), dto.getPayrollMonth())
                .orElse(null);
    }

    private void setRelationships(EmployeeLate entity, EmployeeLateRequestDTO dto) {
        entity.setEmployee(employeeRepository.getReferenceById(dto.getEmpId()));
        if (dto.getLateConfigId() != null) {
            entity.setLateConfig(lateConfigRepository.getReferenceById(dto.getLateConfigId()));
        }
        entity.setCreatedBy(usrRepository.getReferenceById(dto.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(dto.getModifiedBy()));
    }

    private static BigDecimal computeAmount(BigDecimal rate, BigDecimal hours) {
        if (rate == null || hours == null) return BigDecimal.ZERO;
        return rate.multiply(hours).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal orZero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
