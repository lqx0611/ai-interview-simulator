package com.interview.controller;

import com.interview.common.Result;
import com.interview.dto.LoginRequest;
import com.interview.dto.LoginResponse;
import com.interview.dto.RegisterRequest;
import com.interview.dto.UserResponse;
import com.interview.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器 — 处理用户注册、登录等认证相关接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     * POST /api/auth/register
     *
     * @param request 注册参数（username 3-20字符、password 6-20字符、nickname可选）
     * @return 新创建的用户基本信息（不含密码）
     */
    @PostMapping("/register")
    public Result<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        return Result.success(user);
    }

    /**
     * 用户登录
     * POST /api/auth/login
     *
     * @param request 登录参数（username、password）
     * @return JWT Token + 用户基本信息
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse resp = authService.login(request);
        return Result.success(resp);
    }
}
