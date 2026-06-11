package com.payroll.controller;

import com.payroll.enums.ImportEntityType;
import com.payroll.importexport.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.Locale;

@RestController
@RequestMapping("/payroll/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    /**
     * Streams the entity's rows for the given month as XLSX or CSV.
     * The dataset is never fully loaded into memory: JPA streams rows into a
     * windowed {@code SXSSFWorkbook} / {@code CSVWriter} inside the
     * {@link StreamingResponseBody}.
     */
    @GetMapping("/{entity}")
    public ResponseEntity<StreamingResponseBody> export(
            @PathVariable String entity,
            @RequestParam(value = "format", defaultValue = "xlsx") String format,
            @RequestParam("month") String month) {

        ImportEntityType type = ImportEntityType.fromCode(entity);
        boolean csv = "csv".equalsIgnoreCase(format);
        String fileName = type.name().toLowerCase(Locale.ROOT) + "_" + month + "."
                + (csv ? "csv" : "xlsx");

        StreamingResponseBody body = out ->
                exportService.export(type, format, month, out);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .contentType(csv
                        ? MediaType.parseMediaType("text/csv")
                        : MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(body);
    }
}
