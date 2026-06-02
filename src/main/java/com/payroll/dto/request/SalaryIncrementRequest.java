package com.payroll.dto.request;

import com.payroll.enums.IncrementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class SalaryIncrementRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotNull
    private IncrementType type;

    @NotBlank
    private String effectiveMonth;   // YYYY-MM

    private String remarks;

    @NotNull
    private Long createdBy;

    @NotNull
    private Long modifiedBy;

    private List<SalaryIncrementDetailRequest> details;
}
