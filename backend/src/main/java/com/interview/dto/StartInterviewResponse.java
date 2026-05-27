package com.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 开始面试响应体
 */
@Data
@AllArgsConstructor
public class StartInterviewResponse {

    /** 新创建的面试记录ID */
    private Long interviewId;
    /** AI面试官的开场白（含第一个问题） */
    private String openingMessage;
}
