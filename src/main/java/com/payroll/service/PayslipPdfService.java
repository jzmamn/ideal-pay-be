package com.payroll.service;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

/**
 * Generates payslip PDFs from the active HTML template.
 * All methods stream bytes directly to the HTTP response — no in-memory byte[] buffers.
 */
public interface PayslipPdfService {

    /** Single payslip for one payroll run. Uses specified template, or falls back to active. */
    StreamingResponseBody generatePdf(Long runId, Long templateId);

    /** Returns the PDF bytes for a single payslip — used internally (e.g. email attachment). */
    byte[] renderSinglePdfBytes(Long runId, Long templateId);

    /** Merged PDF containing one payslip per selected run, in the order supplied. */
    StreamingResponseBody generatePdfForSelected(List<Long> runIds, Long templateId);

    /** Merged PDF for every run in a given payroll month (e.g. "2026-05"). */
    StreamingResponseBody generatePdfForMonth(String payrollMonth);

    /**
     * Single A4-landscape page with two payslips placed side by side in portrait orientation.
     * Each payslip occupies half the page width (≈ A5 portrait = 148 mm × 210 mm).
     * If runIds has more than 2 entries they are grouped into pairs, one pair per page.
     */
    StreamingResponseBody generatePdf2Up(List<Long> runIds, Long templateId);
}
