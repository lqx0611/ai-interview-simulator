package com.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 结束面试响应体
 */
@Data
@AllArgsConstructor
public class EndInterviewResponse {

    /** 生成的报告ID */
    private Long reportId;
    /** 整体评分（1-10） */
    private BigDecimal overallScore;
    /** AI生成的面试总结 */
    private String summary;
    /** 各知识点评分明细 */
    private List<TopicScoreItem> topicScores;
    /** AI生成的改进建议 */
    private String improvement;
    /** 面试总时长（秒） */
    private int durationSeconds;
    /** 总提问数 */
    private int questionCount;

    /** 知识点评分项 */
    @Data
    @AllArgsConstructor
    public static class TopicScoreItem {
        /** 知识点名称 */
        private String topic;
        /** 评分（1-10） */
        private BigDecimal score;
        /** 评语 */
        private String comment;
        /** 是否为薄弱项 */
        private boolean isWeak;
    }
}
