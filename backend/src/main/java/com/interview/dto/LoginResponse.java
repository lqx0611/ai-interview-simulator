package com.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户登录响应 — 包含JWT Token和用户基本信息
 */
@Data
@AllArgsConstructor
public class LoginResponse {

    /** JWT访问令牌，后续请求通过 Authorization: Bearer xxx 携带 */
    private String token;

    /** 用户基本信息（id、username、nickname），不含密码 */
    private UserResponse user;
}
