package com.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 提交回答请求体
 */
@Data
public class AnswerRequest {

    /** 候选人的回答文本，单次最多2000字符（防止恶意超长输入） */
    @NotBlank(message = "回答内容不能为空")
    @Size(max = 2000, message = "单次回答不能超过2000字符")
    private String content;
}
