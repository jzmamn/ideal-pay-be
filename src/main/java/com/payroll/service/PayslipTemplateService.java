package com.payroll.service;

import com.payroll.dto.request.PayslipTemplateRequestDTO;
import com.payroll.dto.response.PayslipTemplateResponseDTO;

import java.util.List;

public interface PayslipTemplateService {

    List<PayslipTemplateResponseDTO> getAllPayslipTemplates(boolean showDefaultRow, String isActive);

    PayslipTemplateResponseDTO getPayslipTemplateById(Long id);

    PayslipTemplateResponseDTO createPayslipTemplate(PayslipTemplateRequestDTO requestDTO);

    PayslipTemplateResponseDTO updatePayslipTemplate(Long id, PayslipTemplateRequestDTO requestDTO);

    void deletePayslipTemplate(Long id);

    /** Mark a specific template as active. Other templates keep their current status. */
    PayslipTemplateResponseDTO activate(Long id);

    /** Return one active template for default payslip generation. Throws if none exists. */
    PayslipTemplateResponseDTO getActive();
}
