package com.example.traitortracing.repository;

import com.example.traitortracing.entity.Downloads;
import com.example.traitortracing.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DownloadsRepository extends JpaRepository<Downloads, UUID> {
    List<Downloads> findByUser(Users user);
}
