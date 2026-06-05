package com.payroll.service.impl;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.PdfMerger;
import org.jsoup.nodes.Document;
import com.payroll.entity.Company;
import com.payroll.entity.EmpPayrollRun;
import com.payroll.entity.PayslipTemplate;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.repository.CompanyRepository;
import com.payroll.repository.EmpPayrollRunRepository;
import com.payroll.repository.PayslipTemplateRepository;
import com.payroll.service.PayslipPdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generates payslip PDFs using the active HTML template stored in payslip_template.
 *
 * Flow (per payslip):
 *   1. Load EmpPayrollRun + Company
 *   2. Load active HTML template from payslip_template
 *   3. Replace {{TOKEN}} placeholders via PayslipTokenMapper
 *   4. Sanitize rendered HTML with Jsoup (strips XSS vectors)
 *   5. Convert to PDF bytes via iText HtmlConverter (html2pdf)
 *
 * For selected/batch: each payslip is generated as bytes, then all are
 * merged into a single PDF using iText PdfMerger and streamed as one response.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PayslipPdfServiceImpl implements PayslipPdfService {

    private final EmpPayrollRunRepository    runRepo;
    private final CompanyRepository          companyRepo;
    private final PayslipTemplateRepository  templateRepo;
    private final PayslipTokenMapper         tokenMapper;

    // ── Public API ────────────────────────────────────────────────────────

    @Override
    public StreamingResponseBody generatePdf(Long runId, Long templateId) {
        EmpPayrollRun  run      = findRun(runId);
        Company        company  = findCompany();
        PayslipTemplate tmpl    = resolveTemplate(templateId);

        return outputStream -> {
            byte[] pdf = renderToPdf(run, company, tmpl);
            outputStream.write(pdf);
        };
    }

    @Override
    public StreamingResponseBody generatePdfForSelected(List<Long> runIds, Long templateId) {
        if (runIds == null || runIds.isEmpty())
            throw new IllegalArgumentException("At least one run ID is required.");

        Company         company = findCompany();
        PayslipTemplate tmpl    = resolveTemplate(templateId);

        List<byte[]> pages = new ArrayList<>();
        for (Long runId : runIds) {
            EmpPayrollRun run = findRun(runId);
            pages.add(renderToPdf(run, company, tmpl));
        }

        return outputStream -> outputStream.write(merge(pages));
    }

    @Override
    public StreamingResponseBody generatePdfForMonth(String payrollMonth) {
        List<EmpPayrollRun> runs = runRepo.findAllByPayrollMonth(
                payrollMonth, Sort.by("employee.lastName", "employee.firstName"));

        if (runs.isEmpty())
            throw new ResourceNotFoundException(
                    "No payroll runs found for month: " + payrollMonth);

        Company         company = findCompany();
        PayslipTemplate tmpl    = resolveTemplate(null);

        List<byte[]> pages = new ArrayList<>();
        for (EmpPayrollRun run : runs) {
            try {
                pages.add(renderToPdf(run, company, tmpl));
            } catch (Exception ex) {
                // Log and skip failed employees — don't abort the entire batch
                log.error("PDF failed for run={} employee={}: {}",
                        run.getId(), run.getEmployee().getEmployeeNo(), ex.getMessage());
            }
        }

        if (pages.isEmpty())
            throw new IllegalStateException("All payslips failed to generate for month: " + payrollMonth);

        return outputStream -> outputStream.write(merge(pages));
    }

    @Override
    public byte[] renderSinglePdfBytes(Long runId, Long templateId) {
        return renderToPdf(findRun(runId), findCompany(), resolveTemplate(templateId));
    }

    // ── 2-up portrait (side by side) ─────────────────────────────────────

    /**
     * A4 landscape page, two payslips side by side in portrait orientation.
     * Each slot = A5 portrait = 148 mm × 210 mm.
     *
     * If an odd number of runIds is supplied the last page has one payslip
     * on the left and a blank right slot.
     *
     * Pairs are grouped in the order the IDs are supplied.
     * All pairs are merged into a single multi-page PDF.
     */
    @Override
    public StreamingResponseBody generatePdf2Up(List<Long> runIds, Long templateId) {
        if (runIds == null || runIds.isEmpty())
            throw new IllegalArgumentException("At least one run ID is required.");

        Company         company = findCompany();
        PayslipTemplate tmpl    = resolveTemplate(templateId);

        // Render every payslip's body fragment first
        List<String> fragments = new ArrayList<>();
        for (Long runId : runIds) {
            EmpPayrollRun run = findRun(runId);
            fragments.add(renderToFragment(run, company, tmpl));
        }

        // Group into pairs and convert each pair to a PDF page
        List<byte[]> pages = new ArrayList<>();
        for (int i = 0; i < fragments.size(); i += 2) {
            String left  = fragments.get(i);
            String right = (i + 1 < fragments.size()) ? fragments.get(i + 1) : "";
            pages.add(convertToPdf(buildTwoUpPage(left, right)));
        }

        return outputStream -> outputStream.write(merge(pages));
    }

    /**
     * Renders a payslip to an HTML fragment (body content only, no html/head/body tags).
     * This fragment is embedded inside the 2-up page shell.
     */
    private String renderToFragment(EmpPayrollRun run, Company company, PayslipTemplate tmpl) {
        Map<String, String> tokens = tokenMapper.buildTokens(run, company);
        String rendered = replacetokens(tmpl.getHtmlContent(), tokens);
        // Extract only the <body> inner HTML so it can be embedded in the 2-up wrapper
        Document doc = Jsoup.parse(rendered);
        return doc.body().html();
    }

    /**
     * Builds a complete A4-landscape HTML page with two payslip fragments side by side.
     *
     * Layout (table-based — iText-safe):
     *
     *  ┌─────────────────────┬─┬─────────────────────┐
     *  │                     │ │                     │
     *  │   Left payslip      │ │   Right payslip     │
     *  │   (Employee A)      │ │   (Employee B)      │
     *  │   ≈ 140 mm wide     │ │   ≈ 140 mm wide     │
     *  │                     │ │                     │
     *  └─────────────────────┴─┴─────────────────────┘
     *               A4 landscape (297 × 210 mm)
     */
    private static String buildTwoUpPage(String leftFragment, String rightFragment) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8"/>
                  <style>
                    @page {
                      size: A4 landscape;
                      margin: 8mm;
                    }
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    body { width: 100%%; font-family: 'Courier New', Courier, monospace; font-size: 12px; line-height: 1.28; color: #111111; background: #ffffff; }

                    /* ── Two-up shell ── */
                    .sheet {
                      display: table;
                      width: 100%%;
                      table-layout: fixed;
                    }
                    .slip {
                      display: table-cell;
                      width: 50%%;
                      vertical-align: top;
                      padding: 3mm 4mm;
                    }
                    .divider {
                      display: table-cell;
                      width: 1px;
                      padding: 0;
                      border-left: 1px dashed #ccc;
                    }

                    /* ── Inner payslip styles are inherited from the fragment ── */
                  </style>
                </head>
                <body>
                  <div class="sheet">
                    <div class="slip">%s</div>
                    <div class="divider"></div>
                    <div class="slip">%s</div>
                  </div>
                </body>
                </html>
                """.formatted(leftFragment, rightFragment);
    }

    // ── Core render ───────────────────────────────────────────────────────

    /**
     * Renders one payslip to a PDF byte array:
     *   HTML template → token replacement → Jsoup sanitize → iText HTML2PDF
     */
    private byte[] renderToPdf(EmpPayrollRun run, Company company, PayslipTemplate tmpl) {
        Map<String, String> tokens = tokenMapper.buildTokens(run, company);
        String html = replacetokens(tmpl.getHtmlContent(), tokens);
        return convertToPdf(sanitize(html));
    }

    /** Converts a fully-built, sanitized HTML string to a PDF byte array. */
    private byte[] convertToPdf(String html) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            HtmlConverter.convertToPdf(html, baos, new ConverterProperties());
            return baos.toByteArray();
        } catch (Exception ex) {
            log.error("HTML→PDF conversion failed", ex);
            throw new RuntimeException("PDF conversion failed", ex);
        }
    }

    /**
     * Merges multiple single-page (or multi-page) PDFs into one PDF.
     * Each payslip becomes a new page (or pages) in the merged output.
     */
    private byte[] merge(List<byte[]> pdfs) {
        try (ByteArrayOutputStream merged = new ByteArrayOutputStream()) {
            PdfDocument target = new PdfDocument(new PdfWriter(merged));
            PdfMerger merger = new PdfMerger(target);

            for (byte[] pdf : pdfs) {
                PdfDocument src = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdf)));
                merger.merge(src, 1, src.getNumberOfPages());
                src.close();
            }

            target.close();
            return merged.toByteArray();
        } catch (Exception ex) {
            log.error("PDF merge failed", ex);
            throw new RuntimeException("PDF merge failed", ex);
        }
    }

    // ── Token replacement ─────────────────────────────────────────────────

    private static String replacetokens(String html, Map<String, String> tokens) {
        String result = html;
        for (Map.Entry<String, String> entry : tokens.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    // ── Sanitization ──────────────────────────────────────────────────────

    private static final Safelist PAYSLIP_WHITELIST = Safelist.relaxed()
            .addTags("style", "head", "html", "body", "meta")
            .addAttributes(":all", "class", "style", "id", "colspan", "rowspan")
            .addAttributes("meta", "charset", "content", "http-equiv", "name")
            .addAttributes("img",  "src", "alt", "width", "height")
            .removeTags("script")
            .removeAttributes(":all", "onerror", "onload", "onclick", "onmouseover", "onfocus");

    private static String sanitize(String html) {
        // preserveRelativeLinks=true so relative img src values survive
        return Jsoup.clean(html, "", PAYSLIP_WHITELIST,
                new org.jsoup.nodes.Document.OutputSettings().prettyPrint(false));
    }

    // ── Lookups ───────────────────────────────────────────────────────────

    private EmpPayrollRun findRun(Long runId) {
        return runRepo.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", "id", runId));
    }

    /** Single-company system — always returns the first active company record. */
    private Company findCompany() {
        return companyRepo.findAllByIsActive(true, Sort.by("id"))
                .stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No active company found."));
    }

    private PayslipTemplate resolveTemplate(Long templateId) {
        if (templateId != null) {
            return templateRepo.findById(templateId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "PayslipTemplate", "id", templateId));
        }
        return templateRepo.findFirstByIsActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active payslip template found. Upload a template first."));
    }
}
