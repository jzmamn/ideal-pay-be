package com.payroll.mapper;

import com.payroll.dto.request.BankRequestDTO;
import com.payroll.dto.response.BankResponseDTO;
import com.payroll.entity.Bank;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BankMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "isActive",     ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    Bank toEntity(BankRequestDTO requestDTO);

    @Mapping(target = "createdById",        source = "createdBy.id")
    @Mapping(target = "createdByCode",      source = "createdBy.code")
    @Mapping(target = "createdByUserName",  source = "createdBy.userName")
    @Mapping(target = "modifiedById",       source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",     source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.userName")
    BankResponseDTO toResponseDTO(Bank entity);

    List<BankResponseDTO> toResponseDTOList(List<Bank> entities);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "isActive",     ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    void updateEntityFromDTO(BankRequestDTO requestDTO, @MappingTarget Bank entity);
}
