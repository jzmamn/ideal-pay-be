package com.payroll.mapper;

import com.payroll.dto.request.FixedAllowanceRequestDTO;
import com.payroll.dto.response.FixedAllowanceResponseDTO;
import com.payroll.entity.FixedAllowance;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FixedAllowanceMapper {

    // RequestDTO → Entity (for create)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    FixedAllowance toEntity(FixedAllowanceRequestDTO requestDTO);

    // Entity → ResponseDTO
    FixedAllowanceResponseDTO toResponseDTO(FixedAllowance entity);

    // List of entities → List of ResponseDTOs
    List<FixedAllowanceResponseDTO> toResponseDTOList(List<FixedAllowance> entities);

    // RequestDTO → existing Entity (for update), ignoring null values
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromDTO(FixedAllowanceRequestDTO requestDTO, @MappingTarget FixedAllowance entity);
}
