package com.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求 — POST /api/auth/register
 */
@Data
public class RegisterRequest {

    /** 用户名，3-20字符，不可重复 */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度须在3-20字符之间")
    private String username;

    /** 密码，6-20字符 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度须在6-20字符之间")
    private String password;

    /** 昵称（可选，界面展示用，不填则默认使用用户名） */
    @Size(max = 50, message = "昵称最长50字符")
    private String nickname;
}
