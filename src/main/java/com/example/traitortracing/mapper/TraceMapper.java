package com.example.traitortracing.mapper;

import com.example.traitortracing.dto.request.TraceRequest;
import com.example.traitortracing.dto.response.TraceResponse;
import com.example.traitortracing.entity.TraceResults;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TraceMapper {
    TraceResponse toTraceResponse(TraceResults traceResults);

    TraceResults toTraceResults(TraceRequest request);

    void updateTraceResults(@MappingTarget TraceResults traceResults, TraceRequest request);
}