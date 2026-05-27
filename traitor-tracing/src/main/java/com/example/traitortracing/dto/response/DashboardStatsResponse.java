package com.example.traitortracing.dto.response;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class DashboardStatsResponse {
    private long totalOriginalImages;
    private long totalDistributedCopies;
    private long totalViolationsDetected;
    private long totalManagedUsers;
    private List<Map<String, Object>> recentActivities;
    private List<Map<String, Object>> recentAlerts;
}
