package com.payroll.mapper;

import com.payroll.dto.request.UserGroupRequestDTO;
import com.payroll.dto.response.UserGroupResponseDTO;
import com.payroll.entity.UserGroup;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserGroupMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "user",         ignore = true)
    @Mapping(target = "group",        ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    UserGroup toEntity(UserGroupRequestDTO requestDTO);

    @Mapping(target = "userId",             source = "user.id")
    @Mapping(target = "userCode",           source = "user.code")
    @Mapping(target = "userName",           source = "user.name")
    @Mapping(target = "grpId",              source = "group.id")
    @Mapping(target = "grpCode",            source = "group.code")
    @Mapping(target = "grpName",            source = "group.name")
    @Mapping(target = "createdById",        source = "createdBy.id")
    @Mapping(target = "createdByCode",      source = "createdBy.code")
    @Mapping(target = "createdByUserName",  source = "createdBy.username")
    @Mapping(target = "modifiedById",       source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",     source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.username")
    UserGroupResponseDTO toResponseDTO(UserGroup entity);

    List<UserGroupResponseDTO> toResponseDTOList(List<UserGroup> entities);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "user",         ignore = true)
    @Mapping(target = "group",        ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    void updateEntityFromDTO(UserGroupRequestDTO requestDTO, @MappingTarget UserGroup entity);
}
