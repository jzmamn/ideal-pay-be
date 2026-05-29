package com.payroll.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequestDTO {

    @NotBlank(message = "Employee number is required")
    @Size(max = 20, message = "Employee number must not exceed 20 characters")
    private String employeeNo;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    private LocalDate dateOfBirth;

    @Size(max = 15, message = "NIC must not exceed 15 characters")
    private String nic;

    @NotNull(message = "isActive is required")
    private Boolean isActive;

    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    private String remarks;

    @NotBlank(message = "Payroll name is required")
    @Size(max = 150, message = "Payroll name must not exceed 150 characters")
    private String payrollName;

    @Size(max = 50, message = "EPF number must not exceed 50 characters")
    private String epfNo;

    @Size(max = 50, message = "ETF number must not exceed 50 characters")
    private String etfNo;

    private BigDecimal basicSalary;

    @NotNull(message = "Joined date is required")
    private LocalDate joinedDate;

    @NotNull(message = "Employee type is required")
    private Long employeeTypeId;

    private LocalDate contractFrom;

    private LocalDate contractTo;

    @NotNull(message = "Nopay days is required")
    private Long nopayDaysId;

    @NotNull(message = "Job category is required")
    private Long jobCategoryId;

    @NotNull(message = "Designation is required")
    private Long designationId;

    @NotNull(message = "Branch is required")
    private Long branchId;

    private Long gradeId;

    @NotNull(message = "Status is required")
    private Long statusId;

    private LocalDate statDate;

    private LocalDate statFrom;

    private LocalDate statTo;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Email(message = "Email must be a valid email address")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    private String adrsLine1;

    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    private String adrsLine2;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "District must not exceed 100 characters")
    private String district;

    @NotNull(message = "Country is required")
    private Long countryId;

    @Size(max = 150, message = "Contact person must not exceed 150 characters")
    private String contactPerson;

    @Size(max = 255, message = "Contact person address must not exceed 255 characters")
    private String cpAddress;

    @Size(max = 20, message = "Contact person number must not exceed 20 characters")
    private String cpContactNumber;

    @NotNull(message = "createdBy is required")
    private Long createdBy;

    @NotNull(message = "modifiedBy is required")
    private Long modifiedBy;
}
