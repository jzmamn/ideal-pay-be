package com.payroll.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponseDTO {

    private Long id;
    private String code;
    private String employeeNo;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String nic;
    private Boolean isActive;
    private String remarks;
    private String payrollName;
    private String epfNo;
    private String etfNo;
    private BigDecimal basicSalary;
    private LocalDate joinedDate;

    private Long bankId;
    private String bankCode;
    private String bankName;
    private Long bankBranchId;
    private String bankBranchCode;
    private String bankBranchName;
    private String accountNo;

    private Long employeeTypeId;
    private String employeeTypeCode;
    private String employeeTypeName;

    private LocalDate empTypeEndDate;

    private Long nopayDaysId;
    private String nopayDaysCode;
    private String nopayDaysName;

    private Long jobCategoryId;
    private String jobCategoryCode;
    private String jobCategoryName;

    private Long designationId;
    private String designationCode;
    private String designationName;

    private Long branchId;
    private String branchCode;
    private String branchName;

    private Long gradeId;
    private String gradeCode;
    private String gradeName;

    private Long statusId;
    private String statusCode;
    private String statusName;

    private LocalDate statDate;
    private LocalDate statFrom;
    private LocalDate statTo;

    private String phone;
    private String email;
    private String adrsLine1;
    private String adrsLine2;
    private String city;
    private String district;

    private Long countryId;
    private String countryName;

    private String contactPerson;
    private String cpAddress;
    private String cpContactNumber;

    private Long createdById;
    private String createdByCode;
    private String createdByUserName;
    private LocalDateTime createdDate;

    private Long modifiedById;
    private String modifiedByCode;
    private String modifiedByUserName;
    private LocalDateTime modifiedDate;
}
