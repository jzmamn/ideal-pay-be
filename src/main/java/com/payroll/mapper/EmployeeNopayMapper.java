package com.payroll.mapper;

import com.payroll.dto.request.EmployeeNopayRequestDTO;
import com.payroll.dto.response.EmployeeNopayResponseDTO;
import com.payroll.entity.EmployeeNopay;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeNopayMapper {

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "employee",    ignore = true)
    @Mapping(target = "nopayDays",   ignore = true)
    @Mapping(target = "createdBy",   ignore = true)
    @Mapping(target = "modifiedBy",  ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate",ignore = true)
    EmployeeNopay toEntity(EmployeeNopayRequestDTO requestDTO);

    @Mapping(target = "empId",              source = "employee.id")
    @Mapping(target = "empCode",            source = "employee.employeeNo")
    @Mapping(target = "empName",            source = "employee.payrollName")
    @Mapping(target = "nopayId",            source = "nopayDays.id")
    @Mapping(target = "nopayCode",          source = "nopayDays.code")
    @Mapping(target = "nopayName",          source = "nopayDays.name")
    @Mapping(target = "nopayMasterDays",    source = "nopayDays.days")
    @Mapping(target = "createdById",        source = "createdBy.id")
    @Mapping(target = "createdByCode",      source = "createdBy.code")
    @Mapping(target = "createdByUserName",  source = "createdBy.userName")
    @Mapping(target = "modifiedById",       source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",     source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.userName")
    EmployeeNopayResponseDTO toResponseDTO(EmployeeNopay entity);

    List<EmployeeNopayResponseDTO> toResponseDTOList(List<EmployeeNopay> entities);

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "employee",    ignore = true)
    @Mapping(target = "nopayDays",   ignore = true)
    @Mapping(target = "createdBy",   ignore = true)
    @Mapping(target = "modifiedBy",  ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate",ignore = true)
    void updateEntityFromDTO(EmployeeNopayRequestDTO requestDTO, @MappingTarget EmployeeNopay entity);
}
