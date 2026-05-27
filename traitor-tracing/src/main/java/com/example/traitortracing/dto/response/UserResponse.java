package com.example.traitortracing.dto.response;

import lombok.*;

import java.sql.Timestamp;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserResponse {
    private UUID id;
    private String username;
    private String name;
    private String role;
    private Timestamp created_at;
    private String fingerprint_bits;
}
