package com.payroll.mapper;

import com.payroll.dto.request.JobCategoryRequestDTO;
import com.payroll.dto.response.JobCategoryResponseDTO;
import com.payroll.entity.JobCategory;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface JobCategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    JobCategory toEntity(JobCategoryRequestDTO requestDTO);

    JobCategoryResponseDTO toResponseDTO(JobCategory entity);

    List<JobCategoryResponseDTO> toResponseDTOList(List<JobCategory> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromDTO(JobCategoryRequestDTO requestDTO, @MappingTarget JobCategory entity);
}
