package com.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试详情响应体
 * 包含面试基本信息、完整对话消息列表、面试报告（含知识点评分）
 */
@Data
@AllArgsConstructor
public class InterviewDetailResponse {
    private Long id;
    private String direction;
    private String difficulty;
    private String interviewType;
    /** 总评分（面试结束后生成） */
    private BigDecimal totalScore;
    private Integer questionCount;
    private Integer durationSeconds;
    private LocalDateTime createTime;
    /** 完整对话消息列表（按时间顺序） */
    private List<MessageItem> messages;
    /** 面试报告（未结束时为null） */
    private ReportInfo report;

    /** 对话消息项 */
    @Data
    @AllArgsConstructor
    public static class MessageItem {
        /** 角色：interviewer / candidate */
        private String role;
        /** 消息内容 */
        private String content;
        /** 当前考察知识点 */
        private String topic;
        /** 评分（candidate消息的AI评分） */
        private BigDecimal score;
        private LocalDateTime createTime;
    }

    /** 面试报告信息 */
    @Data
    @AllArgsConstructor
    public static class ReportInfo {
        private Long reportId;
        private BigDecimal overallScore;
        private String summary;
        private String improvement;
        /** 知识点评分列表 */
        private List<TopicScoreItem> topicScores;
    }

    /** 知识点评分项 */
    @Data
    @AllArgsConstructor
    public static class TopicScoreItem {
        private String topic;
        private BigDecimal score;
        private String comment;
        private boolean isWeak;
    }
}
