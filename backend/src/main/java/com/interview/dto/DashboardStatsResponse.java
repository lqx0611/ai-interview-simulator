package com.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 看板统计响应体
 * 包含用户面试总览和知识点掌握度统计数据
 */
@Data
@AllArgsConstructor
public class DashboardStatsResponse {
    /** 总面试次数 */
    private int totalInterviews;
    /** 总练习时长（秒） */
    private long totalDurationSeconds;
    /** 各知识点统计列表 */
    private List<TopicStat> topicStats;
    /** 薄弱知识点名称列表 */
    private List<String> weakTopics;

    /** 知识点统计项 */
    @Data
    @AllArgsConstructor
    public static class TopicStat {
        /** 知识点名称 */
        private String topic;
        /** 平均评分（1-10） */
        private BigDecimal avgScore;
        /** 最高评分 */
        private BigDecimal maxScore;
        /** 最低评分 */
        private BigDecimal minScore;
        /** 练习次数 */
        private int practiceCount;
        /** 掌握度等级：proficient(精通) / skilled(熟练) / familiar(了解) / weak(薄弱) */
        private String level;
        /** 最近一次练习时间 */
        private LocalDateTime lastPracticeTime;
    }
}
