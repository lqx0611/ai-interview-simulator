package com.interview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * AI面试官回复解析对象
 * 对应AI返回的JSON结构，用于反序列化AI生成的面试官消息
 */
@Data
public class InterviewerResponse {

    /** AI生成的面试官消息正文 */
    private String content;

    /** 当前考察的知识点名称 */
    @JsonProperty("current_topic")
    private String currentTopic;

    /** 对候选人上一个回答的评分（1-10，开场白时为null） */
    @JsonProperty("answer_score")
    private Integer answerScore;

    /** AI判断的下一步动作：probe(追问) / redirect(引导) / next(下一知识点) / end(结束) */
    private String action;
}
