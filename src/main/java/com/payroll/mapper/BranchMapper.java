package com.payroll.mapper;

import com.payroll.dto.request.BranchRequestDTO;
import com.payroll.dto.response.BranchResponseDTO;
import com.payroll.entity.Branch;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BranchMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    Branch toEntity(BranchRequestDTO requestDTO);

    BranchResponseDTO toResponseDTO(Branch entity);

    List<BranchResponseDTO> toResponseDTOList(List<Branch> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromDTO(BranchRequestDTO requestDTO, @MappingTarget Branch entity);
}
