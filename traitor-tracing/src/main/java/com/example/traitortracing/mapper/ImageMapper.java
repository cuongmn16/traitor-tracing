package com.example.traitortracing.mapper;

import com.example.traitortracing.dto.request.ImageRequest;
import com.example.traitortracing.dto.response.ImageResponse;
import com.example.traitortracing.entity.Images;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "users", ignore = true)
    void toUpdateImages(@MappingTarget Images image, ImageRequest imageRequest);
    ImageResponse toImageResponse(Images images);
}
