package com.payroll.mapper;

import com.payroll.dto.request.EmployeeBonusRequestDTO;
import com.payroll.dto.response.EmployeeBonusResponseDTO;
import com.payroll.entity.EmployeeBonus;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeBonusMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "employee",     ignore = true)
    @Mapping(target = "bonus",        ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    EmployeeBonus toEntity(EmployeeBonusRequestDTO requestDTO);

    @Mapping(target = "empId",              source = "employee.id")
    @Mapping(target = "empCode",            source = "employee.employeeNo")
    @Mapping(target = "empName",            source = "employee.payrollName")
    @Mapping(target = "bonusId",            source = "bonus.id")
    @Mapping(target = "bonusCode",          source = "bonus.code")
    @Mapping(target = "bonusName",          source = "bonus.name")
    @Mapping(target = "createdById",        source = "createdBy.id")
    @Mapping(target = "createdByCode",      source = "createdBy.code")
    @Mapping(target = "createdByUserName",  source = "createdBy.username")
    @Mapping(target = "modifiedById",       source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",     source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.username")
    EmployeeBonusResponseDTO toResponseDTO(EmployeeBonus entity);

    List<EmployeeBonusResponseDTO> toResponseDTOList(List<EmployeeBonus> entities);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "employee",     ignore = true)
    @Mapping(target = "bonus",        ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    void updateEntityFromDTO(EmployeeBonusRequestDTO requestDTO, @MappingTarget EmployeeBonus entity);
}
