package com.payroll.mapper;

import com.payroll.dto.request.EmployeeLoanRequestDTO;
import com.payroll.dto.response.EmployeeLoanResponseDTO;
import com.payroll.entity.EmployeeLoan;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeLoanMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "employee",     ignore = true)
    @Mapping(target = "loan",         ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    EmployeeLoan toEntity(EmployeeLoanRequestDTO requestDTO);

    @Mapping(target = "empId",              source = "employee.id")
    @Mapping(target = "empCode",            source = "employee.employeeNo")
    @Mapping(target = "empName",            source = "employee.payrollName")
    @Mapping(target = "loanId",             source = "loan.id")
    @Mapping(target = "loanCode",           source = "loan.code")
    @Mapping(target = "loanName",           source = "loan.name")
    @Mapping(target = "createdById",        source = "createdBy.id")
    @Mapping(target = "createdByCode",      source = "createdBy.code")
    @Mapping(target = "createdByUserName",  source = "createdBy.userName")
    @Mapping(target = "modifiedById",       source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",     source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.userName")
    EmployeeLoanResponseDTO toResponseDTO(EmployeeLoan entity);

    List<EmployeeLoanResponseDTO> toResponseDTOList(List<EmployeeLoan> entities);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "employee",     ignore = true)
    @Mapping(target = "loan",         ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    void updateEntityFromDTO(EmployeeLoanRequestDTO requestDTO, @MappingTarget EmployeeLoan entity);
}
