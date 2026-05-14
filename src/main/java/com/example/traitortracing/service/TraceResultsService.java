package com.example.traitortracing.service;

import com.example.traitortracing.dto.request.TraceRequest;
import com.example.traitortracing.dto.response.TraceResponse;
import com.example.traitortracing.entity.TraceResults;
import com.example.traitortracing.mapper.TraceMapper;
import com.example.traitortracing.repository.TraceResultsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Thay cho @Autowired thủ công
public class TraceResultsService {
    @Autowired
    private  TraceResultsRepository repository;
    @Autowired
    private  TraceMapper traceMapper;

    public TraceResponse create(TraceRequest request) {
        TraceResults trace = traceMapper.toTraceResults(request);
        return traceMapper.toTraceResponse(repository.save(trace));
    }

    public List<TraceResponse> getAll() {
        return repository.findAll().stream()
                .map(traceMapper::toTraceResponse)
                .collect(Collectors.toList());
    }

    public TraceResponse getById(UUID id) {
        return repository.findById(id)
                .map(traceMapper::toTraceResponse)
                .orElseThrow(() -> new RuntimeException("Trace result not found"));
    }

    public TraceResponse update(UUID id, TraceRequest request) {
        TraceResults trace = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trace result not found"));
        traceMapper.updateTraceResults(trace, request);
        return traceMapper.toTraceResponse(repository.save(trace));
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }
}