package com.payroll.importexport;

import com.payroll.exception.ImportException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Reads the first sheet of an .xlsx workbook. */
@Component
public class XlsxParser implements FileParserStrategy {

    @Override
    public boolean supports(String fileName) {
        return fileName != null && fileName.toLowerCase(Locale.ROOT).endsWith(".xlsx");
    }

    @Override
    public List<Map<String, String>> parse(InputStream in) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(in)) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new ImportException("Workbook contains no sheets");
            }
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new ImportException("Missing header row");
            }

            List<String> headers = new ArrayList<>();
            short lastCell = headerRow.getLastCellNum();
            for (int c = 0; c < lastCell; c++) {
                Cell cell = headerRow.getCell(c);
                headers.add(cell == null ? "" : fmt.formatCellValue(cell).trim());
            }

            List<Map<String, String>> rows = new ArrayList<>();
            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                Map<String, String> values = new LinkedHashMap<>();
                boolean hasValue = false;
                for (int c = 0; c < headers.size(); c++) {
                    if (headers.get(c).isEmpty()) {
                        continue;
                    }
                    Cell cell = row.getCell(c);
                    String value = cell == null ? "" : fmt.formatCellValue(cell).trim();
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
        }
    }
}
