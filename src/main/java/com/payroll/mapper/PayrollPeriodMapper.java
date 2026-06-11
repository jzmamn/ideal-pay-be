package com.payroll.mapper;

import com.payroll.dto.request.PayrollPeriodRequestDTO;
import com.payroll.dto.response.PayrollPeriodResponseDTO;
import com.payroll.entity.PayrollPeriod;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PayrollPeriodMapper {

    @Mapping(target = "id",             ignore = true)
    @Mapping(target = "company",        ignore = true) // resolved in service from companyId
    @Mapping(target = "periodCode",     ignore = true) // auto-generated
    @Mapping(target = "payrollStatus",  ignore = true)
    @Mapping(target = "locked",         ignore = true)
    @Mapping(target = "active",         ignore = true)
    @Mapping(target = "payrollRunDate", ignore = true)
    @Mapping(target = "closedDate",     ignore = true)
    @Mapping(target = "closedBy",       ignore = true)
    @Mapping(target = "createdBy",      ignore = true)
    @Mapping(target = "createdDate",    ignore = true)
    @Mapping(target = "modifiedBy",     ignore = true)
    @Mapping(target = "modifiedDate",   ignore = true)
    PayrollPeriod toEntity(PayrollPeriodRequestDTO requestDTO);

    @Mapping(target = "companyId",          source = "company.id")
    @Mapping(target = "companyName",        source = "company.name")
    @Mapping(target = "closedByUserName",   source = "closedBy.userName")
    @Mapping(target = "createdById",        source = "createdBy.id")
    @Mapping(target = "createdByUserName",  source = "createdBy.userName")
    @Mapping(target = "modifiedById",       source = "modifiedBy.id")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.userName")
    PayrollPeriodResponseDTO toResponseDTO(PayrollPeriod entity);

    List<PayrollPeriodResponseDTO> toResponseDTOList(List<PayrollPeriod> entities);

    /** Only mutable scheduling fields may change on update; status/lock/active are transition-driven. */
    @Mapping(target = "id",             ignore = true)
    @Mapping(target = "company",        ignore = true)
    @Mapping(target = "periodCode",     ignore = true)
    @Mapping(target = "payrollStatus",  ignore = true)
    @Mapping(target = "locked",         ignore = true)
    @Mapping(target = "active",         ignore = true)
    @Mapping(target = "payrollRunDate", ignore = true)
    @Mapping(target = "closedDate",     ignore = true)
    @Mapping(target = "closedBy",       ignore = true)
    @Mapping(target = "createdBy",      ignore = true)
    @Mapping(target = "createdDate",    ignore = true)
    @Mapping(target = "modifiedBy",     ignore = true)
    @Mapping(target = "modifiedDate",   ignore = true)
    void updateEntityFromDTO(PayrollPeriodRequestDTO requestDTO, @MappingTarget PayrollPeriod entity);
}
