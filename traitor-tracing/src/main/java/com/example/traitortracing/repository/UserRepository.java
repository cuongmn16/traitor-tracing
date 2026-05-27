package com.example.traitortracing.repository;

import com.example.traitortracing.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Service
public interface UserRepository extends JpaRepository<Users, UUID> {
    Optional<Users> findByUsername(String username);

    boolean existsByUsername(String username);

    Users getUserByUsername(String username);

    Users getUserById(UUID id);

    long countByRole(String role);
}
