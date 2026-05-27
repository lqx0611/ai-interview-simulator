package com.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class InterviewDetailResponse {
    private Long id;
    private String direction;
    private String difficulty;
    private String interviewType;
    private BigDecimal totalScore;
    private Integer questionCount;
    private Integer durationSeconds;
    private LocalDateTime createTime;
    private List<MessageItem> messages;
    private ReportInfo report;

    @Data
    @AllArgsConstructor
    public static class MessageItem {
        private String role;
        private String content;
        private String topic;
        private BigDecimal score;
        private LocalDateTime createTime;
    }

    @Data
    @AllArgsConstructor
    public static class ReportInfo {
        private Long reportId;
        private BigDecimal overallScore;
        private String summary;
        private String improvement;
        private List<TopicScoreItem> topicScores;
    }

    @Data
    @AllArgsConstructor
    public static class TopicScoreItem {
        private String topic;
        private BigDecimal score;
        private String comment;
        private boolean isWeak;
    }
}
