package com.example.traitortracing.repository;

import com.example.traitortracing.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface UserRepository extends JpaRepository<Users, UUID> {

    boolean existsByUsername(String username);

    Users getUserByUsername(String username);

    Users getUserById(UUID id);
}
