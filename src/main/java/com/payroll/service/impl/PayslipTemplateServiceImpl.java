package com.payroll.service.impl;

import com.payroll.dto.request.PayslipTemplateRequestDTO;
import com.payroll.dto.response.PayslipTemplateResponseDTO;
import com.payroll.entity.PayslipTemplate;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.mapper.PayslipTemplateMapper;
import com.payroll.repository.PayslipTemplateRepository;
import com.payroll.repository.UsrRepository;
import com.payroll.service.PayslipTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PayslipTemplateServiceImpl implements PayslipTemplateService {

    private final PayslipTemplateRepository payslipTemplateRepository;
    private final UsrRepository usrRepository;
    private final PayslipTemplateMapper payslipTemplateMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PayslipTemplateResponseDTO> getAllPayslipTemplates(boolean showDefaultRow, String isActive) {
        if (!isActive.equalsIgnoreCase("true") && !isActive.equalsIgnoreCase("false") && !isActive.equalsIgnoreCase("all")) {
            throw new IllegalArgumentException(
                    "Invalid value for isActive. Accepted values: true, false, all");
        }
        Sort sort = Sort.by("id").ascending();
        List<PayslipTemplate> records = "all".equalsIgnoreCase(isActive)
                ? payslipTemplateRepository.findAll(sort)
                : payslipTemplateRepository.findAllByIsActive(Boolean.parseBoolean(isActive), sort);
        return records.stream()
                .filter(e -> showDefaultRow || e.getId() != -1L)
                .map(payslipTemplateMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PayslipTemplateResponseDTO getPayslipTemplateById(Long id) {
        PayslipTemplate entity = payslipTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PayslipTemplate", "id", id));
        return payslipTemplateMapper.toResponseDTO(entity);
    }

    @Override
    public PayslipTemplateResponseDTO createPayslipTemplate(PayslipTemplateRequestDTO requestDTO) {
        PayslipTemplate entity = payslipTemplateMapper.toEntity(requestDTO);
        entity.setCreatedBy(usrRepository.getReferenceById(requestDTO.getCreatedBy()));
        entity.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        return payslipTemplateMapper.toResponseDTO(payslipTemplateRepository.save(entity));
    }

    @Override
    public PayslipTemplateResponseDTO updatePayslipTemplate(Long id, PayslipTemplateRequestDTO requestDTO) {
        PayslipTemplate existing = payslipTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PayslipTemplate", "id", id));
        payslipTemplateMapper.updateEntityFromDTO(requestDTO, existing);
        if (requestDTO.getModifiedBy() != null) {
            existing.setModifiedBy(usrRepository.getReferenceById(requestDTO.getModifiedBy()));
        }
        return payslipTemplateMapper.toResponseDTO(payslipTemplateRepository.save(existing));
    }

    @Override
    public void deletePayslipTemplate(Long id) {
        PayslipTemplate entity = payslipTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PayslipTemplate", "id", id));
        payslipTemplateRepository.delete(entity);
    }

    @Override
    public PayslipTemplateResponseDTO activate(Long id) {
        PayslipTemplate entity = payslipTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PayslipTemplate", "id", id));
        entity.setIsActive(true);
        return payslipTemplateMapper.toResponseDTO(payslipTemplateRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public PayslipTemplateResponseDTO getActive() {
        return payslipTemplateRepository.findFirstByIsActiveTrue()
                .map(payslipTemplateMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active payslip template found. Please upload and activate a template first."));
    }
}
