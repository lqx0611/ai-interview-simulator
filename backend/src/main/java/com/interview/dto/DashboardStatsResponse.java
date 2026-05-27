package com.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class DashboardStatsResponse {
    private int totalInterviews;
    private long totalDurationSeconds;
    private List<TopicStat> topicStats;
    private List<String> weakTopics;

    @Data
    @AllArgsConstructor
    public static class TopicStat {
        private String topic;
        private BigDecimal avgScore;
        private BigDecimal maxScore;
        private BigDecimal minScore;
        private int practiceCount;
        private String level;
        private LocalDateTime lastPracticeTime;
    }
}
