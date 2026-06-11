package com.payroll.importexport;

import com.payroll.dto.response.ImportRowDTO;
import com.payroll.dto.response.RowErrorDTO;
import lombok.*;

import java.util.List;

/** Outcome of validating a mapped import file. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationResult {

    private List<ImportRowDTO> rows;

    public int getTotalRows() {
        return rows == null ? 0 : rows.size();
    }

    public int getValidRows() {
        return rows == null ? 0 : (int) rows.stream().filter(ImportRowDTO::isValid).count();
    }

    public int getErrorRows() {
        return getTotalRows() - getValidRows();
    }

    public List<RowErrorDTO> getErrors() {
        return rows == null ? List.of()
                : rows.stream().flatMap(r -> r.getErrors().stream()).toList();
    }
}
