package com.interview.controller;

import com.interview.common.Result;
import com.interview.dto.DashboardStatsResponse;
import com.interview.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/api/dashboard/stats")
    public Result<DashboardStatsResponse> stats() {
        return Result.success(dashboardService.getStats());
    }
}
