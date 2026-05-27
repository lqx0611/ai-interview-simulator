package com.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class HistoryItemResponse {
    private Long id;
    private String direction;
    private String difficulty;
    private String interviewType;
    private BigDecimal totalScore;
    private Integer questionCount;
    private Integer durationSeconds;
    private LocalDateTime createTime;
}
