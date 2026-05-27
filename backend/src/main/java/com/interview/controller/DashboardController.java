package com.interview.controller;

import com.interview.common.Result;
import com.interview.dto.DashboardStatsResponse;
import com.interview.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计看板控制器
 * 提供首页数据看板所需的聚合统计接口
 */
@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 知识点掌握度统计
     * 聚合用户所有已完成面试的知识点评分，用于首页看板展示
     *
     * @return 总面试次数、总时长、各知识点统计、薄弱知识点列表
     */
    @GetMapping("/api/dashboard/stats")
    public Result<DashboardStatsResponse> stats() {
        return Result.success(dashboardService.getStats());
    }
}
