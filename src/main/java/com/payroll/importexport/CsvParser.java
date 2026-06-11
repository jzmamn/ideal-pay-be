package com.payroll.importexport;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.payroll.exception.ImportException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Reads a comma-separated file with a header row (UTF-8). */
@Component
public class CsvParser implements FileParserStrategy {

    @Override
    public boolean supports(String fileName) {
        return fileName != null && fileName.toLowerCase(Locale.ROOT).endsWith(".csv");
    }

    @Override
    public List<Map<String, String>> parse(InputStream in) throws IOException {
        try (CSVReader reader = new CSVReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))) {

            String[] headerLine = reader.readNext();
            if (headerLine == null) {
                throw new ImportException("Missing header row");
            }
            List<String> headers = new ArrayList<>();
            for (String h : headerLine) {
                // Strip a UTF-8 BOM if Excel left one on the first header
                headers.add(h == null ? "" : h.replace("\uFEFF", "").trim());
            }

            List<Map<String, String>> rows = new ArrayList<>();
            String[] line;
            while ((line = reader.readNext()) != null) {
                Map<String, String> values = new LinkedHashMap<>();
                boolean hasValue = false;
                for (int c = 0; c < headers.size(); c++) {
                    if (headers.get(c).isEmpty()) {
                        continue;
                    }
                    String value = c < line.length && line[c] != null ? line[c].trim() : "";
                    if (!value.isEmpty()) {
                        hasValue = true;
                    }
                    values.put(headers.get(c), value);
                }
                if (hasValue) {
                    rows.add(values);
                }
            }
            return rows;
        } catch (CsvValidationException e) {
            throw new ImportException("Malformed CSV: " + e.getMessage(), e);
        }
    }
}
