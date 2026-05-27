package com.interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StartInterviewRequest {

    @NotBlank(message = "面试方向不能为空")
    private String direction;

    @NotBlank(message = "难度不能为空")
    private String difficulty;

    @NotBlank(message = "面试类型不能为空")
    private String interviewType;
}
