package com.payroll.mapper;

import com.payroll.dto.request.BankBranchRequestDTO;
import com.payroll.dto.response.BankBranchResponseDTO;
import com.payroll.entity.BankBranch;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BankBranchMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "bank",         ignore = true)
    @Mapping(target = "isActive",     ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    BankBranch toEntity(BankBranchRequestDTO requestDTO);

    @Mapping(target = "bankId",             source = "bank.id")
    @Mapping(target = "bankCode",           source = "bank.code")
    @Mapping(target = "bankName",           source = "bank.name")
    @Mapping(target = "createdById",        source = "createdBy.id")
    @Mapping(target = "createdByCode",      source = "createdBy.code")
    @Mapping(target = "createdByUserName",  source = "createdBy.userName")
    @Mapping(target = "modifiedById",       source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",     source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.userName")
    BankBranchResponseDTO toResponseDTO(BankBranch entity);

    List<BankBranchResponseDTO> toResponseDTOList(List<BankBranch> entities);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "bank",         ignore = true)
    @Mapping(target = "isActive",     ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    void updateEntityFromDTO(BankBranchRequestDTO requestDTO, @MappingTarget BankBranch entity);
}
