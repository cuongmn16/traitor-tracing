package com.example.traitortracing.dto.request;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String username;
    @Column(name = "password_hash")
    private String password_hash ;
    private String role;
    private String fingerprint_bits;
}
