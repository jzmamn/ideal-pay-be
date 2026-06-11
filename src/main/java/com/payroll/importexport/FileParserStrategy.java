package com.payroll.importexport;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Parses an uploaded file into raw rows. The first file row is treated as the
 * header row; every subsequent row becomes a {@code header → cell value} map.
 */
public interface FileParserStrategy {

    /** Whether this parser handles the given file name (by extension). */
    boolean supports(String fileName);

    /**
     * @return one map per data row, keyed by the original column headers,
     *         in file order. Blank rows are skipped.
     */
    List<Map<String, String>> parse(InputStream in) throws IOException;
}
