package com.example.traitortracing.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class StoragePathResolver {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public Path resolveStoredFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("filePath is empty");
        }
        String normalized = filePath.replace('\\', '/');
        if (normalized.startsWith("uploads/")) {
            String relative = normalized.substring("uploads/".length());
            return Path.of(uploadDir).resolve(relative);
        }
        return Path.of(filePath);
    }
}
