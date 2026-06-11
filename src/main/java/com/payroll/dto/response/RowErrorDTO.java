package com.payroll.dto.response;

import lombok.*;

/** A single validation failure: which row, which field, what's wrong. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RowErrorDTO {

    /** 1-based file row number (row 1 is the header). */
    private int rowNum;

    private String field;

    private String message;
}
