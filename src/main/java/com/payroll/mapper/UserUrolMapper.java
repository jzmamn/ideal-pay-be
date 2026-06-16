package com.payroll.mapper;

import com.payroll.dto.request.UserUrolRequestDTO;
import com.payroll.dto.response.UserUrolResponseDTO;
import com.payroll.entity.UserUrol;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserUrolMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "user",         ignore = true)
    @Mapping(target = "urol",         ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    UserUrol toEntity(UserUrolRequestDTO requestDTO);

    @Mapping(target = "userId",             source = "user.id")
    @Mapping(target = "userCode",           source = "user.code")
    @Mapping(target = "userName",           source = "user.name")
    @Mapping(target = "urolId",             source = "urol.id")
    @Mapping(target = "urolCode",           source = "urol.code")
    @Mapping(target = "urolName",           source = "urol.name")
    @Mapping(target = "createdById",        source = "createdBy.id")
    @Mapping(target = "createdByCode",      source = "createdBy.code")
    @Mapping(target = "createdByUserName",  source = "createdBy.username")
    @Mapping(target = "modifiedById",       source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",     source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.username")
    UserUrolResponseDTO toResponseDTO(UserUrol entity);

    List<UserUrolResponseDTO> toResponseDTOList(List<UserUrol> entities);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "user",         ignore = true)
    @Mapping(target = "urol",         ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    void updateEntityFromDTO(UserUrolRequestDTO requestDTO, @MappingTarget UserUrol entity);
}
