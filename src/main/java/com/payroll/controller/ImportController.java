package com.payroll.controller;

import com.payroll.dto.request.ImportRemapRequestDTO;
import com.payroll.dto.response.*;
import com.payroll.enums.ImportEntityType;
import com.payroll.importexport.ImportOrchestrator;
import com.payroll.importexport.ImportRollbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/payroll/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportOrchestrator orchestrator;
    private final ImportRollbackService rollbackService;

    /** Upload + parse + auto-map + validate; stages a 30-minute session. */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO<ImportPreviewResponseDTO>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entity") String entity,
            @RequestParam("payrollMonth") String payrollMonth,
            @RequestParam(value = "userId", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "File parsed and validated",
                orchestrator.upload(file, ImportEntityType.fromCode(entity),
                        payrollMonth, userId)));
    }

    /** Re-validate the staged rows after the user remaps columns. */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponseDTO<ImportPreviewResponseDTO>> validate(
            @Valid @RequestBody ImportRemapRequestDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Rows re-validated",
                orchestrator.revalidate(request.getSessionId(), request.getMapping())));
    }

    /** Persist the valid rows, write import_log, tag rows with import_log_id. */
    @PostMapping("/commit/{sessionId}")
    public ResponseEntity<ApiResponseDTO<ImportCommitResponseDTO>> commit(
            @PathVariable String sessionId,
            @RequestParam(value = "userId", required = false) Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(
                "Import committed",
                orchestrator.commit(sessionId, userId)));
    }

    /** Expected file layout (columns, rules, sample row) per entity. */
    @GetMapping("/formats")
    public ResponseEntity<ApiResponseDTO<List<ImportFormatResponseDTO>>> formats() {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Import formats fetched successfully",
                orchestrator.getFormats()));
    }

    /** Blank template (headers + one sample row) as XLSX or CSV. */
    @GetMapping("/template/{entity}")
    public ResponseEntity<byte[]> template(
            @PathVariable String entity,
            @RequestParam(value = "format", defaultValue = "xlsx") String format) {
        ImportEntityType type = ImportEntityType.fromCode(entity);
        boolean csv = "csv".equalsIgnoreCase(format);
        byte[] body = orchestrator.buildTemplate(type, format);
        String fileName = type.name().toLowerCase(Locale.ROOT) + "_template."
                + (csv ? "csv" : "xlsx");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .contentType(csv
                        ? MediaType.parseMediaType("text/csv")
                        : MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(body);
    }

    /** Past imports, newest first. */
    @GetMapping("/log")
    public ResponseEntity<ApiResponseDTO<List<ImportLogResponseDTO>>> importLogs() {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Import logs fetched successfully",
                orchestrator.getImportLogs()));
    }

    /** Undo a committed import. 409 if any row was already processed. */
    @DeleteMapping("/rollback/{importLogId}")
    public ResponseEntity<ApiResponseDTO<ImportLogResponseDTO>> rollback(
            @PathVariable Long importLogId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Import rolled back",
                rollbackService.rollback(importLogId)));
    }
}
