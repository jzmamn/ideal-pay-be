package com.payroll.service;

import com.payroll.dto.request.BankTransferTemplateRequestDTO;
import com.payroll.dto.request.MarkTransferredRequestDTO;
import com.payroll.dto.response.BankTransferRowDTO;
import com.payroll.dto.response.BankTransferTemplateResponseDTO;

import java.util.List;

public interface BankTransferService {

    // ── Templates ─────────────────────────────────────────────────────────────
    List<BankTransferTemplateResponseDTO> getAllTemplates();
    BankTransferTemplateResponseDTO saveTemplate(BankTransferTemplateRequestDTO request);
    BankTransferTemplateResponseDTO updateTemplate(Long id, BankTransferTemplateRequestDTO request);
    void deleteTemplate(Long id);

    // ── Transfer preview + mark ───────────────────────────────────────────────
    List<BankTransferRowDTO> getTransferPreview(String payrollMonth, List<String> types);
    void markTransferred(MarkTransferredRequestDTO request);
}
