package com.payroll.dto.response;

import lombok.*;

import java.util.List;
import java.util.Map;

/** One staged import row: mapped values plus any validation errors. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportRowDTO {

    /** 1-based file row number (row 1 is the header). */
    private int rowNum;

    /** Expected field → cell value (after column mapping). */
    private Map<String, String> values;

    private List<RowErrorDTO> errors;

    public boolean isValid() {
        return errors == null || errors.isEmpty();
    }
}
