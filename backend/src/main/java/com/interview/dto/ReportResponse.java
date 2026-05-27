package com.interview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * AI面试报告解析对象
 * 对应AI结束面试时返回的报告JSON，用于反序列化
 */
@Data
public class ReportResponse {

    /** 整体评分（1-10） */
    @JsonProperty("overall_score")
    private Integer overallScore;

    /** 面试总结评语 */
    private String summary;

    /** 各知识点评分明细 */
    @JsonProperty("topic_scores")
    private List<TopicScoreItem> topicScores;

    /** 改进建议（针对性学习方向） */
    private String improvement;

    /** 知识点评分项 */
    @Data
    public static class TopicScoreItem {
        /** 知识点名称 */
        private String topic;
        /** 评分（1-10） */
        private Integer score;
        /** 评语 */
        private String comment;
        /** 是否为薄弱项（得分<6） */
        @JsonProperty("is_weak")
        private boolean isWeak;
    }
}
