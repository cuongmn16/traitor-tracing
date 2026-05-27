package com.example.traitortracing.controller;

import com.example.traitortracing.dto.response.ApiResponse;
import com.example.traitortracing.dto.response.DashboardStatsResponse;
import com.example.traitortracing.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin("*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public ApiResponse<DashboardStatsResponse> getStats() {
        ApiResponse<DashboardStatsResponse> response = new ApiResponse<>();
        response.setResult(dashboardService.getStats());
        return response;
    }
}
