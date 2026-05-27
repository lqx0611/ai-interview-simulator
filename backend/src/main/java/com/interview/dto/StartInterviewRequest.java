package com.interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 开始面试请求体
 */
@Data
public class StartInterviewRequest {

    /** 面试方向：java_backend / ai_dev / fullstack */
    @NotBlank(message = "面试方向不能为空")
    private String direction;

    /** 难度等级：junior / mid / senior */
    @NotBlank(message = "难度不能为空")
    private String difficulty;

    /** 面试类型：knowledge / project / comprehensive */
    @NotBlank(message = "面试类型不能为空")
    private String interviewType;
}
