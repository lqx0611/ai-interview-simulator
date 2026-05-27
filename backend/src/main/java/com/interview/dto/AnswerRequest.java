package com.interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 提交回答请求体
 */
@Data
public class AnswerRequest {

    /** 候选人的回答文本 */
    @NotBlank(message = "回答内容不能为空")
    private String content;
}
