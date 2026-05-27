package com.example.traitortracing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users", schema = "spring")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String username;
    @Column(name = "password_hash")
    private String password_hash;
    private String name;
    private String role;
    @CreationTimestamp
    private Timestamp created_at;
    @Column(columnDefinition = "TEXT")
    private String fingerprint_bits;
    @ManyToMany(mappedBy = "users")
    private Set<Images> images;

}
