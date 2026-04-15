package com.example.traitortracing.service;

import com.example.traitortracing.dto.request.TraceRequest;
import com.example.traitortracing.dto.response.TraceResponse;
import com.example.traitortracing.entity.TraceResults;
import com.example.traitortracing.mapper.TraceMapper;
import com.example.traitortracing.repository.TraceResultsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Thay cho @Autowired thủ công
public class TraceResultsService {
    private final TraceResultsRepository repository;
    private final TraceMapper mapper;

    public TraceResponse create(TraceRequest request) {
        TraceResults trace = mapper.toTraceResults(request);
        return mapper.toTraceResponse(repository.save(trace));
    }

    public List<TraceResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toTraceResponse)
                .collect(Collectors.toList());
    }

    public TraceResponse getById(UUID id) {
        return repository.findById(id)
                .map(mapper::toTraceResponse)
                .orElseThrow(() -> new RuntimeException("Trace result not found"));
    }

    public TraceResponse update(UUID id, TraceRequest request) {
        TraceResults trace = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trace result not found"));
        mapper.updateTraceResults(trace, request);
        return mapper.toTraceResponse(repository.save(trace));
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }
}