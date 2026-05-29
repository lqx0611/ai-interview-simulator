package com.interview.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.interview.common.JwtUtil;
import com.interview.dto.LoginRequest;
import com.interview.dto.LoginResponse;
import com.interview.dto.RegisterRequest;
import com.interview.dto.UserResponse;
import com.interview.entity.User;
import com.interview.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证服务 — 处理用户注册和登录业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     * 1. 校验用户名是否已被占用
     * 2. BCrypt加密密码
     * 3. 保存用户记录并返回用户信息
     *
     * @param request 注册请求（username、password、nickname）
     * @return 新创建的用户基本信息（不含密码）
     * @throws IllegalArgumentException 用户名已存在时抛出
     */
    public UserResponse register(RegisterRequest request) {
        // 校验用户名唯一性
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new IllegalArgumentException("用户名已被注册");
        }

        // 构建用户实体，BCrypt加密密码
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);

        log.info("新用户注册成功: username={}, id={}", user.getUsername(), user.getId());
        return UserResponse.from(user);
    }

    /**
     * 用户登录
     * 1. 根据用户名查询用户
     * 2. 使用BCrypt校验密码
     * 3. 生成JWT Token并返回用户信息
     *
     * @param request 登录请求（username、password）
     * @return JWT Token + 用户基本信息
     * @throws IllegalArgumentException 用户名不存在或密码错误时抛出
     */
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        // BCrypt密码比对
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        // 生成JWT Token
        String token = jwtUtil.generateToken(user.getId());

        log.info("用户登录成功: username={}, id={}", user.getUsername(), user.getId());
        return new LoginResponse(token, UserResponse.from(user));
    }
}
