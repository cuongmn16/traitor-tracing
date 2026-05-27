package com.example.traitortracing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraceResponse {
    private UUID id;
    private String extractedFingerprint;
    private UUID matchedUserId;
    private Double confidence;
    private String sourceUrl;
    private LocalDateTime createdAt;
}