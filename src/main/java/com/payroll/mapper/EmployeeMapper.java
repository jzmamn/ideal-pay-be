package com.payroll.mapper;

import com.payroll.dto.request.EmployeeRequestDTO;
import com.payroll.dto.response.EmployeeResponseDTO;
import com.payroll.entity.Employee;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "code",         ignore = true)
    @Mapping(target = "employeeType", ignore = true)
    @Mapping(target = "nopayDays",    ignore = true)
    @Mapping(target = "jobCategory",  ignore = true)
    @Mapping(target = "designation",  ignore = true)
    @Mapping(target = "branch",       ignore = true)
    @Mapping(target = "grade",        ignore = true)
    @Mapping(target = "status",       ignore = true)
    @Mapping(target = "country",      ignore = true)
    @Mapping(target = "bank",         ignore = true)
    @Mapping(target = "bankBranch",   ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    Employee toEntity(EmployeeRequestDTO requestDTO);

    @Mapping(target = "employeeTypeId",    source = "employeeType.id")
    @Mapping(target = "employeeTypeCode",  source = "employeeType.code")
    @Mapping(target = "employeeTypeName",  source = "employeeType.name")
    @Mapping(target = "nopayDaysId",       source = "nopayDays.id")
    @Mapping(target = "nopayDaysCode",     source = "nopayDays.code")
    @Mapping(target = "nopayDaysName",     source = "nopayDays.name")
    @Mapping(target = "jobCategoryId",     source = "jobCategory.id")
    @Mapping(target = "jobCategoryCode",   source = "jobCategory.code")
    @Mapping(target = "jobCategoryName",   source = "jobCategory.name")
    @Mapping(target = "designationId",     source = "designation.id")
    @Mapping(target = "designationCode",   source = "designation.code")
    @Mapping(target = "designationName",   source = "designation.name")
    @Mapping(target = "branchId",          source = "branch.id")
    @Mapping(target = "branchCode",        source = "branch.code")
    @Mapping(target = "branchName",        source = "branch.name")
    @Mapping(target = "gradeId",           source = "grade.id")
    @Mapping(target = "gradeCode",         source = "grade.code")
    @Mapping(target = "gradeName",         source = "grade.name")
    @Mapping(target = "statusId",          source = "status.id")
    @Mapping(target = "statusCode",        source = "status.code")
    @Mapping(target = "statusName",        source = "status.name")
    @Mapping(target = "countryId",         source = "country.id")
    @Mapping(target = "countryName",       source = "country.name")
    @Mapping(target = "bankId",            source = "bank.id")
    @Mapping(target = "bankCode",          source = "bank.code")
    @Mapping(target = "bankName",          source = "bank.name")
    @Mapping(target = "bankBranchId",      source = "bankBranch.id")
    @Mapping(target = "bankBranchCode",    source = "bankBranch.branchCode")
    @Mapping(target = "bankBranchName",    source = "bankBranch.branchName")
    @Mapping(target = "createdById",       source = "createdBy.id")
    @Mapping(target = "createdByCode",     source = "createdBy.code")
    @Mapping(target = "createdByUserName", source = "createdBy.userName")
    @Mapping(target = "modifiedById",      source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",    source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName",source = "modifiedBy.userName")
    EmployeeResponseDTO toResponseDTO(Employee entity);

    List<EmployeeResponseDTO> toResponseDTOList(List<Employee> entities);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "code",         ignore = true)
    @Mapping(target = "employeeType", ignore = true)
    @Mapping(target = "nopayDays",    ignore = true)
    @Mapping(target = "jobCategory",  ignore = true)
    @Mapping(target = "designation",  ignore = true)
    @Mapping(target = "branch",       ignore = true)
    @Mapping(target = "grade",        ignore = true)
    @Mapping(target = "status",       ignore = true)
    @Mapping(target = "country",      ignore = true)
    @Mapping(target = "bank",         ignore = true)
    @Mapping(target = "bankBranch",   ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    void updateEntityFromDTO(EmployeeRequestDTO requestDTO, @MappingTarget Employee entity);
}
