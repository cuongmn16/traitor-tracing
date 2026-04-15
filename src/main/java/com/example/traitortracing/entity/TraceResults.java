package com.example.traitortracing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "trace_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraceResults {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "extracted_fingerprint", columnDefinition = "TEXT")
    private String extractedFingerprint;

    @Column(name = "matched_user_id")
    private UUID matchedUserId;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "source_url", columnDefinition = "TEXT")
    private String sourceUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}