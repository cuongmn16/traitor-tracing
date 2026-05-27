package com.example.traitortracing.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraceRequest {
    private String extractedFingerprint;
    private UUID matchedUserId;
    private Double confidence;
    private String sourceUrl;
}