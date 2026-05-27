package com.example.traitortracing.service;

import com.example.traitortracing.dto.response.DashboardStatsResponse;
import com.example.traitortracing.entity.Downloads;
import com.example.traitortracing.entity.TraceResults;
import com.example.traitortracing.entity.Users;
import com.example.traitortracing.repository.DownloadsRepository;
import com.example.traitortracing.repository.ImagesRepository;
import com.example.traitortracing.repository.TraceResultsRepository;
import com.example.traitortracing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImagesRepository imagesRepository;

    @Autowired
    private DownloadsRepository downloadsRepository;

    @Autowired
    private TraceResultsRepository traceResultsRepository;

    public DashboardStatsResponse getStats() {
        long totalOriginalImages = imagesRepository.count();
        long totalDistributedCopies = downloadsRepository.count();
        long totalViolationsDetected = traceResultsRepository.count();
        long totalManagedUsers = userRepository.countByRole("USER");

        // Lấy danh sách hoạt động gần đây (Tải xuống nhúng watermark)
        List<Downloads> allDownloads = downloadsRepository.findAll();
        allDownloads.sort((d1, d2) -> d2.getDownloaded_at().compareTo(d1.getDownloaded_at()));
        
        List<Map<String, Object>> recentActivities = allDownloads.stream()
                .limit(5)
                .map(download -> {
                    Map<String, Object> activity = new LinkedHashMap<>();
                    activity.put("time", download.getDownloaded_at());
                    activity.put("action", "Embedding");
                    activity.put("content", download.getImage() != null ? download.getImage().getFileName() : "Unknown Image");
                    activity.put("user", download.getUser() != null ? download.getUser().getUsername() : "Unknown User");
                    return activity;
                })
                .collect(Collectors.toList());

        // Lấy danh sách cảnh báo vi phạm gần đây (Trace Results)
        List<TraceResults> allTraces = traceResultsRepository.findAll();
        allTraces.sort((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()));

        List<Map<String, Object>> recentAlerts = allTraces.stream()
                .limit(5)
                .map(trace -> {
                    Map<String, Object> alert = new LinkedHashMap<>();
                    alert.put("id", trace.getId().toString());
                    alert.put("fileName", trace.getSourceUrl());
                    alert.put("confidence", trace.getConfidence());
                    alert.put("createdAt", trace.getCreatedAt());
                    
                    if (trace.getMatchedUserId() != null) {
                        Users user = userRepository.getUserById(trace.getMatchedUserId());
                        if (user != null) {
                            alert.put("leakerUsername", user.getUsername());
                            alert.put("leakerName", user.getName());
                        } else {
                            alert.put("leakerUsername", "ID: " + trace.getMatchedUserId().toString().substring(0, 8));
                            alert.put("leakerName", "Unknown");
                        }
                    } else {
                        alert.put("leakerUsername", "Unknown");
                        alert.put("leakerName", "Unknown");
                    }
                    return alert;
                })
                .collect(Collectors.toList());

        return DashboardStatsResponse.builder()
                .totalOriginalImages(totalOriginalImages)
                .totalDistributedCopies(totalDistributedCopies)
                .totalViolationsDetected(totalViolationsDetected)
                .totalManagedUsers(totalManagedUsers)
                .recentActivities(recentActivities)
                .recentAlerts(recentAlerts)
                .build();
    }
}
