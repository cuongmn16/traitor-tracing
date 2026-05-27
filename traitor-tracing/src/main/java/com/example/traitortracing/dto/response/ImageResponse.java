package com.example.traitortracing.dto.response;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ImageResponse {
    private UUID id;
    private String fileName;
    private String filePath;
    private String phash;
    private Timestamp createdAt;
}
