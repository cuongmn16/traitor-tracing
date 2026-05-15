package com.example.traitortracing.mapper;

import com.example.traitortracing.dto.request.TraceRequest;
import com.example.traitortracing.dto.response.TraceResponse;
import com.example.traitortracing.entity.TraceResults;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TraceMapper {
    TraceResponse toTraceResponse(TraceResults traceResults);

    @org.mapstruct.Mapping(target = "id", ignore = true)
    @org.mapstruct.Mapping(target = "createdAt", ignore = true)
    TraceResults toTraceResults(TraceRequest request);

    @org.mapstruct.Mapping(target = "id", ignore = true)
    @org.mapstruct.Mapping(target = "createdAt", ignore = true)
    void updateTraceResults(@MappingTarget TraceResults traceResults, TraceRequest request);
}