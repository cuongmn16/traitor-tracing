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
    private String fileName; // Thay vì file_name
    private String filePath; // Thay vì file_path
    private String phash;
    private Timestamp createdAt; // Thay vì created_at
}
