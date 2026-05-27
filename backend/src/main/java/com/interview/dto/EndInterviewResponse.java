package com.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class EndInterviewResponse {

    private Long reportId;
    private BigDecimal overallScore;
    private String summary;
    private List<TopicScoreItem> topicScores;
    private String improvement;
    private int durationSeconds;
    private int questionCount;

    @Data
    @AllArgsConstructor
    public static class TopicScoreItem {
        private String topic;
        private BigDecimal score;
        private String comment;
        private boolean isWeak;
    }
}
