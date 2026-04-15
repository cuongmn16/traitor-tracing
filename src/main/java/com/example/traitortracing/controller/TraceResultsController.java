package com.example.traitortracing.controller;

import com.example.traitortracing.dto.request.TraceRequest;
import com.example.traitortracing.dto.response.ApiResponse;
import com.example.traitortracing.dto.response.TraceResponse;
import com.example.traitortracing.service.TraceResultsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/trace-results")
@RequiredArgsConstructor
public class TraceResultsController {
    private final TraceResultsService service;

    @PostMapping
    public ApiResponse<TraceResponse> create(@RequestBody TraceRequest request) {
        return ApiResponse.<TraceResponse>builder()
                .result(service.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<TraceResponse>> getAll() {
        return ApiResponse.<List<TraceResponse>>builder()
                .result(service.getAll())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<TraceResponse> getById(@PathVariable UUID id) {
        return ApiResponse.<TraceResponse>builder()
                .result(service.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<TraceResponse> update(@PathVariable UUID id, @RequestBody TraceRequest request) {
        return ApiResponse.<TraceResponse>builder()
                .result(service.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable UUID id) {
        service.delete(id);
        return ApiResponse.<String>builder()
                .result("Trace result deleted successfully")
                .build();
    }
}