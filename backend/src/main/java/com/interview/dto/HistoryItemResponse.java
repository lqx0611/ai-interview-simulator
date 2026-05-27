package com.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 历史列表项响应体
 */
@Data
@AllArgsConstructor
public class HistoryItemResponse {
    /** 面试记录ID */
    private Long id;
    /** 面试方向 */
    private String direction;
    /** 难度等级 */
    private String difficulty;
    /** 面试类型 */
    private String interviewType;
    /** 总评分 */
    private BigDecimal totalScore;
    /** 提问数 */
    private Integer questionCount;
    /** 面试时长（秒） */
    private Integer durationSeconds;
    /** 面试时间 */
    private LocalDateTime createTime;
}
