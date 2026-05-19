package com.payroll.mapper;

import com.payroll.dto.request.UserRoleRequestDTO;
import com.payroll.dto.response.UserRoleResponseDTO;
import com.payroll.entity.UserRole;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserRoleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    UserRole toEntity(UserRoleRequestDTO requestDTO);

    UserRoleResponseDTO toResponseDTO(UserRole entity);

    List<UserRoleResponseDTO> toResponseDTOList(List<UserRole> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromDTO(UserRoleRequestDTO requestDTO, @MappingTarget UserRole entity);
}
