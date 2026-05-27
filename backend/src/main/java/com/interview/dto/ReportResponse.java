package com.interview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ReportResponse {

    @JsonProperty("overall_score")
    private Integer overallScore;

    private String summary;

    @JsonProperty("topic_scores")
    private List<TopicScoreItem> topicScores;

    private String improvement;

    @Data
    public static class TopicScoreItem {
        private String topic;
        private Integer score;
        private String comment;

        @JsonProperty("is_weak")
        private boolean isWeak;
    }
}
